package com.freightops.accounting.controller;

import com.freightops.accounting.entity.AccountingEntry;
import com.freightops.accounting.entity.JournalEntry;
import com.freightops.accounting.enums.SourceType;
import com.freightops.accounting.service.AccountingReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la consultation et l'interprétation des mouvements
 * comptables
 */
@RestController
@RequestMapping("/api/accounting/reports")
@CrossOrigin(origins = "*")
public class AccountingReportController {

    @Autowired
    private AccountingReportService accountingReportService;

    /**
     * Récupère toutes les écritures comptables avec pagination
     */
    @GetMapping("/journal-entries")
    public ResponseEntity<Page<JournalEntry>> getAllJournalEntries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<JournalEntry> entries = accountingReportService.getAllJournalEntries(pageable);
        return ResponseEntity.ok(entries);
    }

    /**
     * Récupère les écritures par type de source
     */
    @GetMapping("/journal-entries/by-source/{sourceType}")
    public ResponseEntity<List<JournalEntry>> getEntriesBySourceType(
            @PathVariable SourceType sourceType) {

        List<JournalEntry> entries = accountingReportService.getEntriesBySourceType(sourceType);
        return ResponseEntity.ok(entries);
    }

    /**
     * Récupère les écritures pour une période donnée
     */
    @GetMapping("/journal-entries/by-period")
    public ResponseEntity<List<JournalEntry>> getEntriesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<JournalEntry> entries = accountingReportService.getEntriesByDateRange(startDate, endDate);
        return ResponseEntity.ok(entries);
    }

    /**
     * Récupère les mouvements d'un compte spécifique
     */
    @GetMapping("/account-movements/{accountNumber}")
    public ResponseEntity<List<AccountingEntry>> getAccountMovements(
            @PathVariable String accountNumber) {

        List<AccountingEntry> movements = accountingReportService.getAccountMovements(accountNumber);
        return ResponseEntity.ok(movements);
    }

    /**
     * Calcule le solde d'un compte à une date donnée
     */
    @GetMapping("/account-balance/{accountNumber}")
    public ResponseEntity<Map<String, Object>> getAccountBalance(
            @PathVariable String accountNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = date != null ? date : LocalDate.now();
        BigDecimal balance = accountingReportService.getAccountBalanceAtDate(accountNumber, targetDate);

        return ResponseEntity.ok(Map.of(
                "accountNumber", accountNumber,
                "date", targetDate,
                "balance", balance));
    }

    /**
     * Génère un rapport des paiements LTA
     */
    @GetMapping("/lta-payments-report")
    public ResponseEntity<Map<String, Object>> getLTAPaymentReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, Object> report = accountingReportService.getLTAPaymentReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Génère un rapport de trésorerie
     */
    @GetMapping("/treasury-report")
    public ResponseEntity<Map<String, Object>> getTreasuryReport() {
        Map<String, Object> report = accountingReportService.getTreasuryReport();
        return ResponseEntity.ok(report);
    }

    /**
     * Interprète un mouvement comptable
     */
    @GetMapping("/interpret-movement/{entryId}")
    public ResponseEntity<Map<String, String>> interpretMovement(@PathVariable Long entryId) {
        // Cette méthode nécessiterait un repository pour récupérer l'AccountingEntry
        // par ID
        // Pour l'instant, on retourne une réponse générique
        return ResponseEntity.ok(Map.of(
                "interpretation", "Fonctionnalité d'interprétation disponible via le service"));
    }

    /**
     * Valide l'équilibre comptable d'une période
     */
    @GetMapping("/validate-balance")
    public ResponseEntity<Map<String, Object>> validateAccountingBalance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, Object> validation = accountingReportService.validateAccountingBalance(startDate, endDate);
        return ResponseEntity.ok(validation);
    }

    /**
     * Récupère un résumé des comptes principaux
     */
    @GetMapping("/accounts-summary")
    public ResponseEntity<Map<String, Object>> getAccountsSummary() {
        LocalDate today = LocalDate.now();

        // Soldes des comptes principaux
        BigDecimal clientsBalance = accountingReportService.getAccountBalanceAtDate("411", today);
        BigDecimal caisseBalance = accountingReportService.getAccountBalanceAtDate("531", today);
        BigDecimal banqueBalance = accountingReportService.getAccountBalanceAtDate("512", today);
        BigDecimal ventesBalance = accountingReportService.getAccountBalanceAtDate("701", today);

        Map<String, Object> summary = Map.of(
                "date", today,
                "accounts", Map.of(
                        "clients", Map.of("number", "411", "name", "Clients", "balance", clientsBalance),
                        "caisse", Map.of("number", "531", "name", "Caisse", "balance", caisseBalance),
                        "banque", Map.of("number", "512", "name", "Banque", "balance", banqueBalance),
                        "ventes", Map.of("number", "701", "name", "Ventes", "balance", ventesBalance)),
                "totalTreasury", caisseBalance.add(banqueBalance));

        return ResponseEntity.ok(summary);
    }
}
