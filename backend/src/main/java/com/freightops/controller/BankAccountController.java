package com.freightops.controller;

import com.freightops.entity.BankAccount;
import com.freightops.entity.TreasuryTransaction;
import com.freightops.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bank-accounts")
@CrossOrigin(origins = "*")
public class BankAccountController {

    @Autowired
    private BankAccountService bankAccountService;

    @GetMapping
    public ResponseEntity<List<BankAccount>> getAllBankAccounts() {
        List<BankAccount> bankAccounts = bankAccountService.getAllBankAccounts();
        return ResponseEntity.ok(bankAccounts);
    }

    @GetMapping("/active")
    public ResponseEntity<List<BankAccount>> getActiveBankAccounts() {
        List<BankAccount> bankAccounts = bankAccountService.getActiveBankAccounts();
        return ResponseEntity.ok(bankAccounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccount> getBankAccountById(@PathVariable Long id) {
        return bankAccountService.getBankAccountById(id)
                .map(bankAccount -> ResponseEntity.ok(bankAccount))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<BankAccount> getBankAccountByNumber(@PathVariable String accountNumber) {
        return bankAccountService.getBankAccountByNumber(accountNumber)
                .map(bankAccount -> ResponseEntity.ok(bankAccount))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{accountName}")
    public ResponseEntity<BankAccount> getBankAccountByName(@PathVariable String accountName) {
        BankAccount bankAccount = bankAccountService.getBankAccountByName(accountName);
        return bankAccount != null ? ResponseEntity.ok(bankAccount) : ResponseEntity.notFound().build();
    }

    @GetMapping("/bank/{bankName}")
    public ResponseEntity<List<BankAccount>> getBankAccountsByBank(@PathVariable String bankName) {
        List<BankAccount> bankAccounts = bankAccountService.getBankAccountsByBank(bankName);
        return ResponseEntity.ok(bankAccounts);
    }

    @GetMapping("/stats/total-balance")
    public ResponseEntity<BigDecimal> getTotalBankBalance() {
        BigDecimal total = bankAccountService.getTotalBankBalance();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/low-balance")
    public ResponseEntity<List<BankAccount>> getBankAccountsWithLowBalance(@RequestParam BigDecimal threshold) {
        List<BankAccount> bankAccounts = bankAccountService.getBankAccountsWithLowBalance(threshold);
        return ResponseEntity.ok(bankAccounts);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getBankAccountBalance(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            BigDecimal balance = bankAccountService.getBankAccountBalance(id, date);
            return ResponseEntity.ok(balance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TreasuryTransaction>> getBankAccountTransactions(@PathVariable Long id) {
        try {
            List<TreasuryTransaction> transactions = bankAccountService.getBankAccountTransactions(id);
            return ResponseEntity.ok(transactions);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/transactions/date-range")
    public ResponseEntity<List<TreasuryTransaction>> getBankAccountTransactionsByDateRange(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<TreasuryTransaction> transactions = bankAccountService.getBankAccountTransactionsByDateRange(id, startDate, endDate);
            return ResponseEntity.ok(transactions);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createBankAccount(@Valid @RequestBody BankAccount bankAccount) {
        try {
            BankAccount createdBankAccount = bankAccountService.createBankAccount(bankAccount);
            return ResponseEntity.ok(createdBankAccount);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBankAccount(@PathVariable Long id, @Valid @RequestBody BankAccount bankAccountDetails) {
        try {
            BankAccount updatedBankAccount = bankAccountService.updateBankAccount(id, bankAccountDetails);
            return ResponseEntity.ok(updatedBankAccount);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBankAccount(@PathVariable Long id) {
        try {
            bankAccountService.deleteBankAccount(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateBankAccount(@PathVariable Long id) {
        try {
            BankAccount activatedBankAccount = bankAccountService.activateBankAccount(id);
            return ResponseEntity.ok(activatedBankAccount);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateBankAccount(@PathVariable Long id) {
        try {
            BankAccount deactivatedBankAccount = bankAccountService.deactivateBankAccount(id);
            return ResponseEntity.ok(deactivatedBankAccount);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/adjust-balance")
    public ResponseEntity<?> adjustBalance(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String reason = (String) request.get("reason");
            BankAccount adjustedBankAccount = bankAccountService.adjustBalance(id, amount, reason);
            return ResponseEntity.ok(adjustedBankAccount);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
