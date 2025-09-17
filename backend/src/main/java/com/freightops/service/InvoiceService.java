package com.freightops.service;

import com.freightops.entity.Invoice;
import com.freightops.entity.InvoiceItem;
import com.freightops.entity.Client;
import com.freightops.entity.Payment;
import com.freightops.repository.InvoiceRepository;
import com.freightops.repository.InvoiceItemRepository;
import com.freightops.repository.ClientRepository;
import com.freightops.repository.PaymentRepository;
import com.freightops.enums.InvoiceStatus;
import com.freightops.enums.InvoiceType;
import com.freightops.accounting.config.AccountingEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.Year;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AccountingEventPublisher accountingEventPublisher;

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public List<Invoice> getInvoicesByClient(Long clientId) {
        return invoiceRepository.findByClientIdOrderByInvoiceDateDesc(clientId);
    }

    public List<Invoice> getInvoicesByType(InvoiceType type) {
        return invoiceRepository.findByType(type);
    }

    public List<Invoice> getInvoicesByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status);
    }

    public List<Invoice> getOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices(LocalDate.now());
    }

    public List<Invoice> getInvoicesByDateRange(LocalDate startDate, LocalDate endDate) {
        return invoiceRepository.findByInvoiceDateBetween(startDate, endDate);
    }

    public List<Invoice> getInvoicesByTypeAndDateRange(InvoiceType type, LocalDate startDate, LocalDate endDate) {
        return invoiceRepository.findByTypeAndInvoiceDateBetween(type, startDate, endDate);
    }

    public Optional<Invoice> getInvoiceById(Long id) {
        return invoiceRepository.findById(id);
    }

    public Optional<Invoice> getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber);
    }

    public BigDecimal getTotalPaidAmount(LocalDate startDate, LocalDate endDate) {
        BigDecimal total = invoiceRepository.sumPaidAmountByDateRange(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalRemainingAmount() {
        BigDecimal total = invoiceRepository.sumRemainingAmount();
        return total != null ? total : BigDecimal.ZERO;
    }

    public Invoice createInvoice(Invoice invoice) {
        // Générer le numéro de facture automatiquement
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isEmpty()) {
            invoice.setInvoiceNumber(generateInvoiceNumber());
        }

        // Vérifier que le client existe
        Client client = clientRepository.findById(invoice.getClient().getId())
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        invoice.setClient(client);

        // Calculer les montants
        calculateInvoiceAmounts(invoice);

        // Sauvegarder la facture
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Publier l'événement comptable pour génération automatique des écritures
        try {
            accountingEventPublisher.publishInvoiceCreatedEvent(savedInvoice, "SYSTEM");
        } catch (Exception e) {
            // Log l'erreur mais ne pas faire échouer la création de facture
            System.err.println("Erreur lors de la publication de l'événement comptable: " + e.getMessage());
        }

        return savedInvoice;
    }

    public Invoice updateInvoice(Long id, Invoice invoiceDetails) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID: " + id));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new RuntimeException("Impossible de modifier une facture payée");
        }

        invoice.setInvoiceDate(invoiceDetails.getInvoiceDate());
        invoice.setDueDate(invoiceDetails.getDueDate());
        invoice.setDescription(invoiceDetails.getDescription());
        invoice.setAmountExcludingTax(invoiceDetails.getAmountExcludingTax());
        invoice.setTaxAmount(invoiceDetails.getTaxAmount());
        invoice.setStatus(invoiceDetails.getStatus());

        calculateInvoiceAmounts(invoice);

        return invoiceRepository.save(invoice);
    }

    public void deleteInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID: " + id));

        if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.PARTIALLY_PAID) {
            throw new RuntimeException("Impossible de supprimer une facture avec des paiements");
        }

        invoiceRepository.delete(invoice);
    }

    public Invoice addItemToInvoice(Long invoiceId, InvoiceItem item) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new RuntimeException("Impossible de modifier une facture payée");
        }

        item.setInvoice(invoice);
        invoiceItemRepository.save(item);

        // Recalculer les montants de la facture
        recalculateInvoiceAmounts(invoice);

        return invoiceRepository.save(invoice);
    }

    public Invoice removeItemFromInvoice(Long invoiceId, Long itemId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new RuntimeException("Impossible de modifier une facture payée");
        }

        InvoiceItem item = invoiceItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec l'ID: " + itemId));

        if (!item.getInvoice().getId().equals(invoiceId)) {
            throw new RuntimeException("Cet article n'appartient pas à cette facture");
        }

        invoiceItemRepository.delete(item);

        // Recalculer les montants de la facture
        recalculateInvoiceAmounts(invoice);

        return invoiceRepository.save(invoice);
    }

    public Invoice updateInvoiceStatus(Long id, InvoiceStatus status) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID: " + id));

        InvoiceStatus oldStatus = invoice.getStatus();
        invoice.setStatus(status);
        Invoice updatedInvoice = invoiceRepository.save(invoice);

        // Si la facture passe au statut SENT ou CONFIRMED, publier l'événement
        // comptable
        if ((oldStatus == InvoiceStatus.DRAFT || oldStatus == InvoiceStatus.PENDING) &&
                (status == InvoiceStatus.SENT || status == InvoiceStatus.CONFIRMED)) {
            try {
                accountingEventPublisher.publishInvoiceCreatedEvent(updatedInvoice, "SYSTEM");
            } catch (Exception e) {
                System.err.println("Erreur lors de la publication de l'événement comptable: " + e.getMessage());
            }
        }

        return updatedInvoice;
    }

    public void updateOverdueInvoices() {
        List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoices(LocalDate.now());
        for (Invoice invoice : overdueInvoices) {
            if (invoice.getStatus() == InvoiceStatus.SENT || invoice.getStatus() == InvoiceStatus.PARTIALLY_PAID) {
                invoice.setStatus(InvoiceStatus.OVERDUE);
                invoiceRepository.save(invoice);
            }
        }
    }

    public void updateInvoicePaymentStatus(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'ID: " + invoiceId));

        List<Payment> payments = paymentRepository.findByInvoice(invoice);
        BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getStatus().name().equals("COMPLETED"))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.setPaidAmount(totalPaid);
        invoiceRepository.save(invoice);
    }

    private void calculateInvoiceAmounts(Invoice invoice) {
        if (invoice.getAmountExcludingTax() != null) {
            if (invoice.getTaxAmount() == null) {
                invoice.setTaxAmount(BigDecimal.ZERO);
            }
            if (invoice.getPaidAmount() == null) {
                invoice.setPaidAmount(BigDecimal.ZERO);
            }

            invoice.setTotalAmount(invoice.getAmountExcludingTax().add(invoice.getTaxAmount()));
            invoice.setRemainingAmount(invoice.getTotalAmount().subtract(invoice.getPaidAmount()));
        }
    }

    private void recalculateInvoiceAmounts(Invoice invoice) {
        List<InvoiceItem> items = invoiceItemRepository.findByInvoice(invoice);

        BigDecimal totalExcludingTax = items.stream()
                .map(InvoiceItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTaxAmount = items.stream()
                .map(InvoiceItem::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.setAmountExcludingTax(totalExcludingTax);
        invoice.setTaxAmount(totalTaxAmount);
        calculateInvoiceAmounts(invoice);
    }

    public String generateInvoiceNumber() {
        int currentYear = Year.now().getValue();
        Long count = invoiceRepository.countByYear(currentYear);
        return String.format("FAC-%d-%05d", currentYear, count + 1);
    }
}
