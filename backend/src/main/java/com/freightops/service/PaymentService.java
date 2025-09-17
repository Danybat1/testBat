package com.freightops.service;

import com.freightops.entity.Payment;
import com.freightops.entity.Invoice;
import com.freightops.entity.CashBox;
import com.freightops.entity.BankAccount;
import com.freightops.entity.TreasuryTransaction;
import com.freightops.repository.PaymentRepository;
import com.freightops.repository.InvoiceRepository;
import com.freightops.repository.CashBoxRepository;
import com.freightops.repository.BankAccountRepository;
import com.freightops.enums.PaymentStatus;
import com.freightops.enums.TransactionType;
import com.freightops.accounting.config.AccountingEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CashBoxRepository cashBoxRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private TreasuryTransactionService treasuryTransactionService;

    @Autowired
    private AccountingEventPublisher accountingEventPublisher;

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getPaymentsByInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée"));
        return paymentRepository.findByInvoice(invoice);
    }

    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    public List<Payment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return paymentRepository.findByPaymentDateBetween(startDate, endDate);
    }

    public List<Payment> getPaymentsByCashBox(Long cashBoxId) {
        CashBox cashBox = cashBoxRepository.findById(cashBoxId)
                .orElseThrow(() -> new RuntimeException("Caisse non trouvée"));
        return paymentRepository.findByCashBox(cashBox);
    }

    public List<Payment> getPaymentsByBankAccount(Long bankAccountId) {
        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new RuntimeException("Compte bancaire non trouvé"));
        return paymentRepository.findByBankAccount(bankAccount);
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public BigDecimal getTotalPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        BigDecimal total = paymentRepository.sumCompletedPaymentsByDateRange(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    public Payment createPayment(Payment payment) {
        // Vérifier que la facture existe
        Invoice invoice = invoiceRepository.findById(payment.getInvoice().getId())
                .orElseThrow(() -> new RuntimeException("Facture non trouvée"));

        // Vérifier que le montant ne dépasse pas le montant restant
        if (payment.getAmount().compareTo(invoice.getRemainingAmount()) > 0) {
            throw new RuntimeException("Le montant du paiement dépasse le montant restant de la facture");
        }

        // Vérifier qu'une caisse ou un compte bancaire est spécifié
        if (payment.getCashBox() == null && payment.getBankAccount() == null) {
            throw new RuntimeException("Une caisse ou un compte bancaire doit être spécifié");
        }

        payment.setInvoice(invoice);
        Payment savedPayment = paymentRepository.save(payment);

        // Créer la transaction de trésorerie correspondante
        createTreasuryTransactionForPayment(savedPayment);

        // Mettre à jour le statut de paiement de la facture
        invoiceService.updateInvoicePaymentStatus(invoice.getId());

        return savedPayment;
    }

    public Payment updatePayment(Long id, Payment paymentDetails) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé avec l'ID: " + id));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new RuntimeException("Impossible de modifier un paiement complété");
        }

        payment.setAmount(paymentDetails.getAmount());
        payment.setPaymentDate(paymentDetails.getPaymentDate());
        payment.setPaymentMethod(paymentDetails.getPaymentMethod());
        payment.setReference(paymentDetails.getReference());
        payment.setNotes(paymentDetails.getNotes());
        payment.setCashBox(paymentDetails.getCashBox());
        payment.setBankAccount(paymentDetails.getBankAccount());

        return paymentRepository.save(payment);
    }

    public Payment completePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé avec l'ID: " + id));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new RuntimeException("Ce paiement est déjà complété");
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        Payment savedPayment = paymentRepository.save(payment);

        // Mettre à jour les soldes de caisse/compte bancaire
        updateAccountBalances(payment);

        // Mettre à jour le statut de paiement de la facture
        invoiceService.updateInvoicePaymentStatus(payment.getInvoice().getId());

        // Publier l'événement comptable pour génération automatique des écritures
        try {
            accountingEventPublisher.publishPaymentReceivedEvent(savedPayment, "SYSTEM");
        } catch (Exception e) {
            // Log l'erreur mais ne pas faire échouer la finalisation du paiement
            System.err.println("Erreur lors de la publication de l'événement comptable: " + e.getMessage());
        }

        return savedPayment;
    }

    public Payment cancelPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé avec l'ID: " + id));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new RuntimeException("Impossible d'annuler un paiement complété");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        return paymentRepository.save(payment);
    }

    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé avec l'ID: " + id));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new RuntimeException("Impossible de supprimer un paiement complété");
        }

        paymentRepository.delete(payment);
    }

    private void createTreasuryTransactionForPayment(Payment payment) {
        TreasuryTransaction transaction = new TreasuryTransaction();
        transaction.setReference("PAY-" + payment.getId());
        transaction.setType(TransactionType.INCOME);
        transaction.setAmount(payment.getAmount());
        transaction.setTransactionDate(payment.getPaymentDate());
        transaction.setDescription("Paiement facture " + payment.getInvoice().getInvoiceNumber());
        transaction.setCategory("Paiement client");
        transaction.setCashBox(payment.getCashBox());
        transaction.setBankAccount(payment.getBankAccount());
        transaction.setPayment(payment);

        treasuryTransactionService.createTransaction(transaction);
    }

    private void updateAccountBalances(Payment payment) {
        if (payment.getCashBox() != null) {
            CashBox cashBox = payment.getCashBox();
            cashBox.setCurrentBalance(cashBox.getCurrentBalance().add(payment.getAmount()));
            cashBoxRepository.save(cashBox);
        }

        if (payment.getBankAccount() != null) {
            BankAccount bankAccount = payment.getBankAccount();
            bankAccount.setCurrentBalance(bankAccount.getCurrentBalance().add(payment.getAmount()));
            bankAccountRepository.save(bankAccount);
        }
    }
}
