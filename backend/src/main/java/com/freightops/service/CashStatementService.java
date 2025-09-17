package com.freightops.service;

import com.freightops.dto.CashOperationDTO;
import com.freightops.dto.CashStatementDTO;
import com.freightops.entity.TreasuryTransaction;
import com.freightops.entity.Payment;
import com.freightops.entity.CashBox;
import com.freightops.enums.TransactionType;
import com.freightops.repository.TreasuryTransactionRepository;
import com.freightops.repository.PaymentRepository;
import com.freightops.repository.CashBoxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CashStatementService {

    @Autowired
    private TreasuryTransactionRepository treasuryTransactionRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CashBoxRepository cashBoxRepository;

    public CashStatementDTO getCashStatement(LocalDate startDate, LocalDate endDate, String currency, Long cashBoxId) {
        System.out.println("Début getCashStatement - startDate: " + startDate + ", endDate: " + endDate + ", currency: "
                + currency + ", cashBoxId: " + cashBoxId);

        // Get cash box or default to first active cash box
        CashBox cashBox = getCashBox(cashBoxId);
        System.out.println("Caisse trouvée: " + cashBox.getName() + " (ID: " + cashBox.getId() + ")");

        // Calculate opening balance (balance at start of period)
        BigDecimal openingBalance = calculateOpeningBalance(cashBox, startDate, currency);
        System.out.println("Solde d'ouverture calculé: " + openingBalance);

        // Get all cash operations for the period
        List<CashOperationDTO> operations = getCashOperations(cashBox, startDate, endDate, currency);
        System.out.println("Nombre d'opérations trouvées: " + operations.size());

        // Calculate totals
        BigDecimal totalEncaissements = operations.stream()
                .filter(op -> op.getOperationType().contains("ENCAISSEMENT"))
                .map(CashOperationDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDecaissements = operations.stream()
                .filter(op -> op.getOperationType().equals("DECAISSEMENT"))
                .map(CashOperationDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate running balances for each operation
        calculateRunningBalances(operations, openingBalance);

        // Calculate closing balance
        BigDecimal closingBalance = openingBalance.add(totalEncaissements).subtract(totalDecaissements);

        // Create and return DTO
        CashStatementDTO statement = new CashStatementDTO();
        statement.setCashBoxName(cashBox.getName());
        statement.setStartDate(startDate);
        statement.setEndDate(endDate);
        statement.setCurrency(currency);
        statement.setOpeningBalance(openingBalance);
        statement.setTotalEncaissements(totalEncaissements);
        statement.setTotalDecaissements(totalDecaissements);
        statement.setClosingBalance(closingBalance);
        statement.setOperations(operations);

        System.out.println("Statement créé avec succès - Encaissements: " + totalEncaissements + ", Décaissements: "
                + totalDecaissements);
        return statement;
    }

    private CashBox getCashBox(Long cashBoxId) {
        System.out.println("Recherche de la caisse avec ID: " + cashBoxId);
        if (cashBoxId != null) {
            return cashBoxRepository.findById(cashBoxId)
                    .orElseThrow(() -> new RuntimeException("Caisse non trouvée avec ID: " + cashBoxId));
        }
        // Return first active cash box as default
        System.out.println("Recherche de la première caisse active...");
        return cashBoxRepository.findFirstByActiveTrue()
                .orElseThrow(() -> new RuntimeException("Aucune caisse active trouvée"));
    }

    private BigDecimal calculateOpeningBalance(CashBox cashBox, LocalDate startDate, String currency) {
        // Get all transactions before the start date
        List<TreasuryTransaction> previousTransactions = treasuryTransactionRepository
                .findByCashBoxAndTransactionDateBeforeOrderByTransactionDateAsc(cashBox, startDate);

        BigDecimal balance = cashBox.getInitialBalance();

        for (TreasuryTransaction transaction : previousTransactions) {
            if (transaction.getType() == TransactionType.INCOME) {
                balance = balance.add(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.EXPENSE) {
                balance = balance.subtract(transaction.getAmount());
            }
        }

        return balance;
    }

    private List<CashOperationDTO> getCashOperations(CashBox cashBox, LocalDate startDate, LocalDate endDate,
            String currency) {
        List<CashOperationDTO> operations = new ArrayList<>();

        // Get treasury transactions for the period
        List<TreasuryTransaction> transactions = treasuryTransactionRepository
                .findByCashBoxAndTransactionDateBetweenOrderByTransactionDateAsc(cashBox, startDate, endDate);

        for (TreasuryTransaction transaction : transactions) {
            CashOperationDTO operation = new CashOperationDTO();
            operation.setId(transaction.getId());
            operation.setReference(transaction.getReference());
            operation.setOperationDate(transaction.getTransactionDate());
            operation.setOperationType(mapTransactionType(transaction.getType()));
            operation.setClientSupplier(extractClientSupplier(transaction));
            operation.setDescription(transaction.getDescription());
            operation.setAmount(transaction.getAmount());
            operation.setCurrency(currency);
            operation.setCategory(transaction.getCategory());
            operation.setPaymentMethod("CASH");

            operations.add(operation);
        }

        // Get LTA payments for the period
        List<Payment> ltaPayments = paymentRepository
                .findByCashBoxAndPaymentDateBetweenOrderByPaymentDateAsc(cashBox, startDate, endDate);

        for (Payment payment : ltaPayments) {
            CashOperationDTO operation = new CashOperationDTO();
            operation.setId(payment.getId());
            operation.setReference(payment.getReference() != null ? payment.getReference() : "PAY-" + payment.getId());
            operation.setOperationDate(payment.getPaymentDate());
            operation.setOperationType("ENCAISSEMENT_LTA");
            operation.setClientSupplier(extractClientFromPayment(payment));
            operation.setDescription("Encaissement LTA - " + (payment.getNotes() != null ? payment.getNotes() : ""));
            operation.setAmount(payment.getAmount());
            operation.setCurrency(currency);
            operation.setPaymentMethod(
                    payment.getPaymentMethod() != null ? payment.getPaymentMethod().toString() : "CASH");

            operations.add(operation);
        }

        // Sort all operations by date
        return operations.stream()
                .sorted((a, b) -> a.getOperationDate().compareTo(b.getOperationDate()))
                .collect(Collectors.toList());
    }

    private void calculateRunningBalances(List<CashOperationDTO> operations, BigDecimal openingBalance) {
        BigDecimal runningBalance = openingBalance;

        for (CashOperationDTO operation : operations) {
            if (operation.getOperationType().contains("ENCAISSEMENT")) {
                runningBalance = runningBalance.add(operation.getAmount());
            } else if (operation.getOperationType().equals("DECAISSEMENT")) {
                runningBalance = runningBalance.subtract(operation.getAmount());
            }
            operation.setBalance(runningBalance);
        }
    }

    private String mapTransactionType(TransactionType type) {
        switch (type) {
            case INCOME:
                return "ENCAISSEMENT_CLIENT";
            case EXPENSE:
                return "DECAISSEMENT";
            case TRANSFER:
                return "VIREMENT";
            default:
                return "AUTRE";
        }
    }

    private String extractClientSupplier(TreasuryTransaction transaction) {
        // Extract client/supplier from description or notes
        String description = transaction.getDescription();
        if (description != null && description.contains(" - ")) {
            String[] parts = description.split(" - ");
            if (parts.length > 1) {
                return parts[0];
            }
        }
        return "Client/Fournisseur";
    }

    private String extractClientFromPayment(Payment payment) {
        // Try to get client name from payment
        if (payment.getInvoice() != null && payment.getInvoice().getClient() != null) {
            return payment.getInvoice().getClient().getName();
        }
        return "Client LTA";
    }
}
