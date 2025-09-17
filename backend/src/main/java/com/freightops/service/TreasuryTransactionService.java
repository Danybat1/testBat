package com.freightops.service;

import com.freightops.entity.TreasuryTransaction;
import com.freightops.entity.CashBox;
import com.freightops.entity.BankAccount;
import com.freightops.repository.TreasuryTransactionRepository;
import com.freightops.repository.CashBoxRepository;
import com.freightops.repository.BankAccountRepository;
import com.freightops.enums.TransactionType;
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
public class TreasuryTransactionService {

    @Autowired
    private TreasuryTransactionRepository treasuryTransactionRepository;

    @Autowired
    private CashBoxRepository cashBoxRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    public List<TreasuryTransaction> getAllTransactions() {
        return treasuryTransactionRepository.findAll();
    }

    public List<TreasuryTransaction> getTransactionsByType(TransactionType type) {
        return treasuryTransactionRepository.findByType(type);
    }

    public List<TreasuryTransaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return treasuryTransactionRepository.findByTransactionDateBetween(startDate, endDate);
    }

    public List<TreasuryTransaction> getTransactionsByCashBox(Long cashBoxId) {
        return treasuryTransactionRepository.findByCashBoxIdOrderByTransactionDateDesc(cashBoxId);
    }

    public List<TreasuryTransaction> getTransactionsByBankAccount(Long bankAccountId) {
        return treasuryTransactionRepository.findByBankAccountIdOrderByTransactionDateDesc(bankAccountId);
    }

    public List<TreasuryTransaction> getTransactionsByCategory(String category) {
        return treasuryTransactionRepository.findByCategoryOrderByTransactionDateDesc(category);
    }

    public List<String> getAllCategories() {
        return treasuryTransactionRepository.findDistinctCategories();
    }

    public Optional<TreasuryTransaction> getTransactionById(Long id) {
        return treasuryTransactionRepository.findById(id);
    }

    public Optional<TreasuryTransaction> getTransactionByReference(String reference) {
        return treasuryTransactionRepository.findByReference(reference);
    }

    public BigDecimal getTotalIncomeByDateRange(LocalDate startDate, LocalDate endDate) {
        BigDecimal total = treasuryTransactionRepository.sumIncomeByDateRange(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalExpenseByDateRange(LocalDate startDate, LocalDate endDate) {
        BigDecimal total = treasuryTransactionRepository.sumExpenseByDateRange(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    public TreasuryTransaction createTransaction(TreasuryTransaction transaction) {
        // Générer une référence automatiquement si non fournie
        if (transaction.getReference() == null || transaction.getReference().isEmpty()) {
            transaction.setReference(generateTransactionReference(transaction.getType()));
        }

        // Vérifier l'unicité de la référence
        if (treasuryTransactionRepository.existsByReference(transaction.getReference())) {
            throw new RuntimeException("Une transaction avec cette référence existe déjà");
        }

        // Vérifier qu'une caisse ou un compte bancaire est spécifié (sauf pour les transferts)
        if (transaction.getType() != TransactionType.TRANSFER) {
            if (transaction.getCashBox() == null && transaction.getBankAccount() == null) {
                throw new RuntimeException("Une caisse ou un compte bancaire doit être spécifié");
            }
        }

        TreasuryTransaction savedTransaction = treasuryTransactionRepository.save(transaction);

        // Mettre à jour les soldes
        updateAccountBalances(savedTransaction);

        return savedTransaction;
    }

    public TreasuryTransaction updateTransaction(Long id, TreasuryTransaction transactionDetails) {
        TreasuryTransaction transaction = treasuryTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction non trouvée avec l'ID: " + id));

        // Annuler l'impact sur les soldes de l'ancienne transaction
        reverseAccountBalances(transaction);

        // Mettre à jour les détails
        transaction.setType(transactionDetails.getType());
        transaction.setAmount(transactionDetails.getAmount());
        transaction.setTransactionDate(transactionDetails.getTransactionDate());
        transaction.setDescription(transactionDetails.getDescription());
        transaction.setCategory(transactionDetails.getCategory());
        transaction.setCashBox(transactionDetails.getCashBox());
        transaction.setBankAccount(transactionDetails.getBankAccount());
        transaction.setDestinationCashBox(transactionDetails.getDestinationCashBox());
        transaction.setDestinationBankAccount(transactionDetails.getDestinationBankAccount());
        transaction.setNotes(transactionDetails.getNotes());

        TreasuryTransaction savedTransaction = treasuryTransactionRepository.save(transaction);

        // Appliquer l'impact sur les soldes de la nouvelle transaction
        updateAccountBalances(savedTransaction);

        return savedTransaction;
    }

    public void deleteTransaction(Long id) {
        TreasuryTransaction transaction = treasuryTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction non trouvée avec l'ID: " + id));

        // Vérifier si la transaction est liée à un paiement
        if (transaction.getPayment() != null) {
            throw new RuntimeException("Impossible de supprimer une transaction liée à un paiement");
        }

        // Annuler l'impact sur les soldes
        reverseAccountBalances(transaction);

        treasuryTransactionRepository.delete(transaction);
    }

    public TreasuryTransaction createTransfer(Long sourceCashBoxId, Long sourceAccountId, 
                                            Long destCashBoxId, Long destAccountId, 
                                            BigDecimal amount, String description) {
        
        if ((sourceCashBoxId == null && sourceAccountId == null) || 
            (destCashBoxId == null && destAccountId == null)) {
            throw new RuntimeException("Source et destination doivent être spécifiées pour un transfert");
        }

        TreasuryTransaction transfer = new TreasuryTransaction();
        transfer.setReference(generateTransactionReference(TransactionType.TRANSFER));
        transfer.setType(TransactionType.TRANSFER);
        transfer.setAmount(amount);
        transfer.setTransactionDate(LocalDate.now());
        transfer.setDescription(description);
        transfer.setCategory("Transfert");

        if (sourceCashBoxId != null) {
            CashBox sourceCashBox = cashBoxRepository.findById(sourceCashBoxId)
                    .orElseThrow(() -> new RuntimeException("Caisse source non trouvée"));
            transfer.setCashBox(sourceCashBox);
        }

        if (sourceAccountId != null) {
            BankAccount sourceAccount = bankAccountRepository.findById(sourceAccountId)
                    .orElseThrow(() -> new RuntimeException("Compte source non trouvé"));
            transfer.setBankAccount(sourceAccount);
        }

        if (destCashBoxId != null) {
            CashBox destCashBox = cashBoxRepository.findById(destCashBoxId)
                    .orElseThrow(() -> new RuntimeException("Caisse destination non trouvée"));
            transfer.setDestinationCashBox(destCashBox);
        }

        if (destAccountId != null) {
            BankAccount destAccount = bankAccountRepository.findById(destAccountId)
                    .orElseThrow(() -> new RuntimeException("Compte destination non trouvé"));
            transfer.setDestinationBankAccount(destAccount);
        }

        return createTransaction(transfer);
    }

    private void updateAccountBalances(TreasuryTransaction transaction) {
        switch (transaction.getType()) {
            case INCOME:
                addToBalance(transaction.getCashBox(), transaction.getBankAccount(), transaction.getAmount());
                break;
            case EXPENSE:
                subtractFromBalance(transaction.getCashBox(), transaction.getBankAccount(), transaction.getAmount());
                break;
            case TRANSFER:
                // Débiter la source
                subtractFromBalance(transaction.getCashBox(), transaction.getBankAccount(), transaction.getAmount());
                // Créditer la destination
                addToBalance(transaction.getDestinationCashBox(), transaction.getDestinationBankAccount(), transaction.getAmount());
                break;
        }
    }

    private void reverseAccountBalances(TreasuryTransaction transaction) {
        switch (transaction.getType()) {
            case INCOME:
                subtractFromBalance(transaction.getCashBox(), transaction.getBankAccount(), transaction.getAmount());
                break;
            case EXPENSE:
                addToBalance(transaction.getCashBox(), transaction.getBankAccount(), transaction.getAmount());
                break;
            case TRANSFER:
                // Annuler le débit de la source
                addToBalance(transaction.getCashBox(), transaction.getBankAccount(), transaction.getAmount());
                // Annuler le crédit de la destination
                subtractFromBalance(transaction.getDestinationCashBox(), transaction.getDestinationBankAccount(), transaction.getAmount());
                break;
        }
    }

    private void addToBalance(CashBox cashBox, BankAccount bankAccount, BigDecimal amount) {
        if (cashBox != null) {
            cashBox.setCurrentBalance(cashBox.getCurrentBalance().add(amount));
            cashBoxRepository.save(cashBox);
        }
        if (bankAccount != null) {
            bankAccount.setCurrentBalance(bankAccount.getCurrentBalance().add(amount));
            bankAccountRepository.save(bankAccount);
        }
    }

    private void subtractFromBalance(CashBox cashBox, BankAccount bankAccount, BigDecimal amount) {
        if (cashBox != null) {
            BigDecimal newBalance = cashBox.getCurrentBalance().subtract(amount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Solde insuffisant dans la caisse " + cashBox.getName());
            }
            cashBox.setCurrentBalance(newBalance);
            cashBoxRepository.save(cashBox);
        }
        if (bankAccount != null) {
            bankAccount.setCurrentBalance(bankAccount.getCurrentBalance().subtract(amount));
            bankAccountRepository.save(bankAccount);
        }
    }

    private String generateTransactionReference(TransactionType type) {
        int currentYear = Year.now().getValue();
        String prefix = switch (type) {
            case INCOME -> "REC";
            case EXPENSE -> "DEP";
            case TRANSFER -> "TRF";
        };
        
        long count = treasuryTransactionRepository.findAll().stream()
                .filter(t -> t.getReference().startsWith(prefix + "-" + currentYear))
                .count();
        
        return String.format("%s-%d-%05d", prefix, currentYear, count + 1);
    }
}
