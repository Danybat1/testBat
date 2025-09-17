package com.freightops.accounting.controller;

import com.freightops.accounting.entity.Account;
import com.freightops.accounting.entity.JournalEntry;
import com.freightops.accounting.entity.FiscalYear;
import com.freightops.accounting.enums.AccountType;
import com.freightops.accounting.service.AccountService;
import com.freightops.accounting.service.JournalEntryService;
import com.freightops.accounting.service.FiscalYearService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Contrôleur REST pour la consultation des données comptables
 * API en lecture seule pour les rapports et consultations
 */
@RestController
@RequestMapping("/api/accounting")
@CrossOrigin(origins = "*")
public class AccountingController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private FiscalYearService fiscalYearService;

    // ========== PLAN COMPTABLE ==========

    /**
     * Récupère tous les comptes actifs
     */
    @GetMapping("/accounts")
    public ResponseEntity<List<Account>> getAllAccounts() {
        List<Account> accounts = accountService.getAllActiveAccounts();
        return ResponseEntity.ok(accounts);
    }

    /**
     * Récupère un compte par son ID
     */
    @GetMapping("/accounts/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        // Cette méthode nécessiterait d'être ajoutée au service
        return ResponseEntity.notFound().build();
    }

    /**
     * Récupère les comptes par type
     */
    @GetMapping("/accounts/type/{accountType}")
    public ResponseEntity<List<Account>> getAccountsByType(@PathVariable AccountType accountType) {
        List<Account> accounts = accountService.getAccountsByType(accountType);
        return ResponseEntity.ok(accounts);
    }

    /**
     * Récupère les comptes racines
     */
    @GetMapping("/accounts/root")
    public ResponseEntity<List<Account>> getRootAccounts() {
        List<Account> accounts = accountService.getRootAccounts();
        return ResponseEntity.ok(accounts);
    }

    /**
     * Recherche des comptes par nom
     */
    @GetMapping("/accounts/search")
    public ResponseEntity<List<Account>> searchAccounts(@RequestParam String query) {
        List<Account> accounts = accountService.searchAccountsByName(query);
        return ResponseEntity.ok(accounts);
    }

    /**
     * Récupère le solde total par type de compte
     */
    @GetMapping("/accounts/balance/{accountType}")
    public ResponseEntity<BigDecimal> getTotalBalanceByType(@PathVariable AccountType accountType) {
        BigDecimal balance = accountService.getTotalBalanceByAccountType(accountType);
        return ResponseEntity.ok(balance);
    }

    // ========== ÉCRITURES COMPTABLES ==========

    /**
     * Récupère les écritures par exercice comptable
     */
    @GetMapping("/journal-entries/fiscal-year/{fiscalYearId}")
    public ResponseEntity<List<JournalEntry>> getJournalEntriesByFiscalYear(@PathVariable Long fiscalYearId) {
        List<JournalEntry> entries = journalEntryService.getJournalEntriesByFiscalYear(fiscalYearId);
        return ResponseEntity.ok(entries);
    }

    /**
     * Récupère les écritures par période
     */
    @GetMapping("/journal-entries/period")
    public ResponseEntity<List<JournalEntry>> getJournalEntriesByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<JournalEntry> entries = journalEntryService.getJournalEntriesByDateRange(startDate, endDate);
        return ResponseEntity.ok(entries);
    }

    /**
     * Récupère une écriture par son numéro
     */
    @GetMapping("/journal-entries/number/{entryNumber}")
    public ResponseEntity<JournalEntry> getJournalEntryByNumber(@PathVariable String entryNumber) {
        JournalEntry entry = journalEntryService.getJournalEntryByNumber(entryNumber);
        if (entry != null) {
            return ResponseEntity.ok(entry);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Récupère les écritures liées à une source
     */
    @GetMapping("/journal-entries/source")
    public ResponseEntity<List<JournalEntry>> getJournalEntriesBySource(
            @RequestParam String sourceType,
            @RequestParam Long sourceId) {
        List<JournalEntry> entries = journalEntryService.getJournalEntriesBySource(sourceType, sourceId);
        return ResponseEntity.ok(entries);
    }

    // ========== EXERCICES COMPTABLES ==========

    /**
     * Récupère tous les exercices comptables
     */
    @GetMapping("/fiscal-years")
    public ResponseEntity<List<FiscalYear>> getAllFiscalYears() {
        List<FiscalYear> fiscalYears = fiscalYearService.getAllFiscalYears();
        return ResponseEntity.ok(fiscalYears);
    }

    /**
     * Récupère l'exercice comptable actuel
     */
    @GetMapping("/fiscal-years/current")
    public ResponseEntity<FiscalYear> getCurrentFiscalYear() {
        FiscalYear currentFiscalYear = fiscalYearService.getCurrentFiscalYear();
        if (currentFiscalYear != null) {
            return ResponseEntity.ok(currentFiscalYear);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Récupère un exercice par son année
     */
    @GetMapping("/fiscal-years/year/{year}")
    public ResponseEntity<FiscalYear> getFiscalYearByYear(@PathVariable Integer year) {
        FiscalYear fiscalYear = fiscalYearService.getFiscalYearByYear(year);
        if (fiscalYear != null) {
            return ResponseEntity.ok(fiscalYear);
        }
        return ResponseEntity.notFound().build();
    }

    // ========== RAPPORTS ET STATISTIQUES ==========

    /**
     * Récupère les statistiques du tableau de bord
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        DashboardStats stats = new DashboardStats();

        // Calcul des statistiques
        stats.cashBalance = accountService.getTotalBalanceByAccountType(AccountType.ASSET)
                .subtract(accountService.getTotalBalanceByAccountType(AccountType.LIABILITY));
        stats.bankBalance = accountService.getTotalBalanceByAccountType(AccountType.ASSET);
        stats.totalInvoicesIssued = BigDecimal.valueOf(150); // Mock data - à remplacer par vraie logique
        stats.totalInvoicesCollected = BigDecimal.valueOf(120); // Mock data
        stats.pendingEntries = 5L; // Mock data
        stats.unbalancedEntries = 0L; // Mock data
        stats.currentFiscalYear = "2024";

        return ResponseEntity.ok(stats);
    }

    /**
     * Valide l'équilibre du plan comptable
     */
    @GetMapping("/reports/balance-validation")
    public ResponseEntity<Boolean> validateChartOfAccountsBalance() {
        boolean isBalanced = accountService.validateChartOfAccountsBalance();
        return ResponseEntity.ok(isBalanced);
    }

    /**
     * Récupère un résumé des soldes par type de compte
     */
    @GetMapping("/reports/balance-summary")
    public ResponseEntity<List<BalanceSummaryItem>> getBalanceSummary() {
        List<BalanceSummaryItem> summaryList = new ArrayList<>();

        // Créer un résumé pour chaque type de compte
        for (AccountType accountType : AccountType.values()) {
            BalanceSummaryItem item = new BalanceSummaryItem();
            item.accountNumber = accountType.name();
            item.accountName = getAccountTypeLabel(accountType);
            item.accountType = accountType;
            item.totalDebit = BigDecimal.ZERO;
            item.totalCredit = BigDecimal.ZERO;
            item.balance = accountService.getTotalBalanceByAccountType(accountType);
            item.currency = "CDF";
            summaryList.add(item);
        }

        return ResponseEntity.ok(summaryList);
    }

    private String getAccountTypeLabel(AccountType accountType) {
        switch (accountType) {
            case ASSET:
                return "Actifs";
            case LIABILITY:
                return "Passifs";
            case EQUITY:
                return "Capitaux propres";
            case REVENUE:
                return "Produits";
            case EXPENSE:
                return "Charges";
            default:
                return accountType.name();
        }
    }

    /**
     * Classe interne pour les statistiques du tableau de bord
     */
    public static class DashboardStats {
        public BigDecimal cashBalance;
        public BigDecimal bankBalance;
        public BigDecimal totalInvoicesIssued;
        public BigDecimal totalInvoicesCollected;
        public Long pendingEntries;
        public Long unbalancedEntries;
        public String currentFiscalYear;
    }

    /**
     * Classe interne pour les éléments du résumé des soldes
     */
    public static class BalanceSummaryItem {
        public String accountNumber;
        public String accountName;
        public AccountType accountType;
        public BigDecimal totalDebit;
        public BigDecimal totalCredit;
        public BigDecimal balance;
        public String currency;
    }

    /**
     * Classe interne pour le résumé des soldes (deprecated)
     */
    public static class BalanceSummary {
        public BigDecimal totalAssets;
        public BigDecimal totalLiabilities;
        public BigDecimal totalEquity;
        public BigDecimal totalRevenues;
        public BigDecimal totalExpenses;
        public boolean isBalanced;
    }
}
