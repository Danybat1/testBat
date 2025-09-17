package com.freightops.service;

import com.freightops.entity.BankAccount;
import com.freightops.entity.TreasuryTransaction;
import com.freightops.repository.BankAccountRepository;
import com.freightops.repository.TreasuryTransactionRepository;
import com.freightops.enums.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BankAccountService {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TreasuryTransactionRepository treasuryTransactionRepository;

    public List<BankAccount> getAllBankAccounts() {
        return bankAccountRepository.findAll();
    }

    public List<BankAccount> getActiveBankAccounts() {
        return bankAccountRepository.findActiveBankAccountsOrderByAccountName();
    }

    public Optional<BankAccount> getBankAccountById(Long id) {
        return bankAccountRepository.findById(id);
    }

    public Optional<BankAccount> getBankAccountByNumber(String accountNumber) {
        return bankAccountRepository.findByAccountNumber(accountNumber);
    }

    public BankAccount getBankAccountByName(String accountName) {
        return bankAccountRepository.findByAccountNameIgnoreCase(accountName);
    }

    public List<BankAccount> getBankAccountsByBank(String bankName) {
        return bankAccountRepository.findByBankNameIgnoreCase(bankName);
    }

    public BigDecimal getTotalBankBalance() {
        BigDecimal total = bankAccountRepository.sumCurrentBalanceOfActiveBankAccounts();
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<BankAccount> getBankAccountsWithLowBalance(BigDecimal threshold) {
        return bankAccountRepository.findBankAccountsWithLowBalance(threshold);
    }

    public BankAccount createBankAccount(BankAccount bankAccount) {
        if (bankAccountRepository.existsByAccountNumber(bankAccount.getAccountNumber())) {
            throw new RuntimeException("Un compte avec ce numéro existe déjà");
        }

        if (bankAccountRepository.existsByAccountNameIgnoreCase(bankAccount.getAccountName())) {
            throw new RuntimeException("Un compte avec ce nom existe déjà");
        }

        if (bankAccount.getCurrentBalance() == null) {
            bankAccount.setCurrentBalance(bankAccount.getInitialBalance());
        }

        return bankAccountRepository.save(bankAccount);
    }

    public BankAccount updateBankAccount(Long id, BankAccount bankAccountDetails) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte bancaire non trouvé avec l'ID: " + id));

        // Vérifier si le numéro de compte existe déjà pour un autre compte
        Optional<BankAccount> existingByNumber = bankAccountRepository.findByAccountNumber(bankAccountDetails.getAccountNumber());
        if (existingByNumber.isPresent() && !existingByNumber.get().getId().equals(id)) {
            throw new RuntimeException("Un compte avec ce numéro existe déjà");
        }

        // Vérifier si le nom de compte existe déjà pour un autre compte
        BankAccount existingByName = bankAccountRepository.findByAccountNameIgnoreCase(bankAccountDetails.getAccountName());
        if (existingByName != null && !existingByName.getId().equals(id)) {
            throw new RuntimeException("Un compte avec ce nom existe déjà");
        }

        bankAccount.setAccountName(bankAccountDetails.getAccountName());
        bankAccount.setAccountNumber(bankAccountDetails.getAccountNumber());
        bankAccount.setBankName(bankAccountDetails.getBankName());
        bankAccount.setIban(bankAccountDetails.getIban());
        bankAccount.setSwift(bankAccountDetails.getSwift());
        bankAccount.setActive(bankAccountDetails.getActive());

        return bankAccountRepository.save(bankAccount);
    }

    public void deleteBankAccount(Long id) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte bancaire non trouvé avec l'ID: " + id));

        // Vérifier s'il y a des transactions
        List<TreasuryTransaction> transactions = treasuryTransactionRepository.findByBankAccount(bankAccount);
        if (!transactions.isEmpty()) {
            throw new RuntimeException("Impossible de supprimer un compte avec des transactions. Désactivez-le plutôt.");
        }

        bankAccountRepository.delete(bankAccount);
    }

    public BankAccount activateBankAccount(Long id) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte bancaire non trouvé avec l'ID: " + id));
        bankAccount.setActive(true);
        return bankAccountRepository.save(bankAccount);
    }

    public BankAccount deactivateBankAccount(Long id) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte bancaire non trouvé avec l'ID: " + id));
        bankAccount.setActive(false);
        return bankAccountRepository.save(bankAccount);
    }

    public BankAccount adjustBalance(Long id, BigDecimal amount, String reason) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte bancaire non trouvé avec l'ID: " + id));

        if (!bankAccount.getActive()) {
            throw new RuntimeException("Impossible d'ajuster le solde d'un compte inactif");
        }

        BigDecimal newBalance = bankAccount.getCurrentBalance().add(amount);
        bankAccount.setCurrentBalance(newBalance);
        return bankAccountRepository.save(bankAccount);
    }

    public BigDecimal getBankAccountBalance(Long id, LocalDate date) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte bancaire non trouvé avec l'ID: " + id));

        // Calculer le solde à une date donnée
        BigDecimal income = treasuryTransactionRepository.sumByBankAccountAndTypeAndDateRange(
                id, TransactionType.INCOME, LocalDate.MIN, date);
        BigDecimal expense = treasuryTransactionRepository.sumByBankAccountAndTypeAndDateRange(
                id, TransactionType.EXPENSE, LocalDate.MIN, date);

        if (income == null) income = BigDecimal.ZERO;
        if (expense == null) expense = BigDecimal.ZERO;

        return bankAccount.getInitialBalance().add(income).subtract(expense);
    }

    public List<TreasuryTransaction> getBankAccountTransactions(Long id) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte bancaire non trouvé avec l'ID: " + id));
        return treasuryTransactionRepository.findByBankAccountIdOrderByTransactionDateDesc(id);
    }

    public List<TreasuryTransaction> getBankAccountTransactionsByDateRange(Long id, LocalDate startDate, LocalDate endDate) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte bancaire non trouvé avec l'ID: " + id));
        
        return treasuryTransactionRepository.findByBankAccount(bankAccount).stream()
                .filter(t -> !t.getTransactionDate().isBefore(startDate) && !t.getTransactionDate().isAfter(endDate))
                .toList();
    }
}
