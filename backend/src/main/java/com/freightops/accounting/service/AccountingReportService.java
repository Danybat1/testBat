package com.freightops.accounting.service;

import com.freightops.accounting.entity.Account;
import com.freightops.accounting.entity.AccountingEntry;
import com.freightops.accounting.entity.JournalEntry;
import com.freightops.accounting.enums.AccountType;
import com.freightops.accounting.enums.SourceType;
import com.freightops.accounting.repository.AccountRepository;
import com.freightops.accounting.repository.AccountingEntryRepository;
import com.freightops.accounting.repository.JournalEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Service pour consulter et interpréter les mouvements comptables
 * Fournit des méthodes pour analyser les écritures et générer des rapports
 */
@Service
public class AccountingReportService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private AccountingEntryRepository accountingEntryRepository;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Récupère toutes les écritures comptables avec pagination
     */
    public Page<JournalEntry> getAllJournalEntries(Pageable pageable) {
        return journalEntryRepository.findAll(pageable);
    }

    /**
     * Récupère les écritures par type de source (LTA_PAYMENT, INVOICE, etc.)
     */
    public List<JournalEntry> getEntriesBySourceType(SourceType sourceType) {
        return journalEntryRepository.findBySourceType(sourceType);
    }

    /**
     * Récupère les écritures pour une période donnée
     */
    public List<JournalEntry> getEntriesByDateRange(LocalDate startDate, LocalDate endDate) {
        return journalEntryRepository.findByEntryDateBetween(startDate, endDate);
    }

    /**
     * Récupère les mouvements d'un compte spécifique
     */
    public List<AccountingEntry> getAccountMovements(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            return List.of();
        }
        return accountingEntryRepository.findByAccountOrderByJournalEntry_EntryDateDesc(account);
    }

    /**
     * Calcule le solde d'un compte à une date donnée
     */
    public BigDecimal getAccountBalanceAtDate(String accountNumber, LocalDate date) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            return BigDecimal.ZERO;
        }

        List<AccountingEntry> entries = accountingEntryRepository
                .findByAccountAndJournalEntry_EntryDateLessThanEqual(account, date);

        BigDecimal balance = BigDecimal.ZERO;
        for (AccountingEntry entry : entries) {
            if (account.getAccountType().increasesWithDebit()) {
                balance = balance.add(entry.getDebitAmount()).subtract(entry.getCreditAmount());
            } else {
                balance = balance.add(entry.getCreditAmount()).subtract(entry.getDebitAmount());
            }
        }
        return balance;
    }

    /**
     * Génère un rapport des paiements LTA
     */
    public Map<String, Object> getLTAPaymentReport(LocalDate startDate, LocalDate endDate) {
        List<JournalEntry> ltaPayments = journalEntryRepository
                .findBySourceTypeAndEntryDateBetween(SourceType.LTA_PAYMENT, startDate, endDate);

        BigDecimal totalAmount = ltaPayments.stream()
                .flatMap(je -> je.getAccountingEntries().stream())
                .filter(ae -> ae.getAccount().getAccountNumber().startsWith("5")) // Comptes de trésorerie
                .map(AccountingEntry::getDebitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> paymentsByMethod = ltaPayments.stream()
                .collect(Collectors.groupingBy(
                        je -> extractPaymentMethod(je.getDescription()),
                        Collectors.reducing(BigDecimal.ZERO,
                                je -> je.getTotalDebit(),
                                BigDecimal::add)));

        Map<String, Object> report = new HashMap<>();
        report.put("period", startDate + " au " + endDate);
        report.put("totalPayments", ltaPayments.size());
        report.put("totalAmount", totalAmount);
        report.put("paymentsByMethod", paymentsByMethod);
        report.put("entries", ltaPayments);

        return report;
    }

    /**
     * Génère un rapport de trésorerie
     */
    public Map<String, Object> getTreasuryReport() {
        // Solde caisse (531)
        BigDecimal caisseBalance = getAccountBalanceAtDate("531", LocalDate.now());

        // Solde banque (512)
        BigDecimal banqueBalance = getAccountBalanceAtDate("512", LocalDate.now());

        // Mouvements récents de trésorerie
        List<AccountingEntry> recentCaisseMovements = getRecentAccountMovements("531", 10);
        List<AccountingEntry> recentBanqueMovements = getRecentAccountMovements("512", 10);

        Map<String, Object> report = new HashMap<>();
        report.put("caisseBalance", caisseBalance);
        report.put("banqueBalance", banqueBalance);
        report.put("totalTreasury", caisseBalance.add(banqueBalance));
        report.put("recentCaisseMovements", recentCaisseMovements);
        report.put("recentBanqueMovements", recentBanqueMovements);

        return report;
    }

    /**
     * Interprète le type de mouvement comptable
     */
    public String interpretMovement(AccountingEntry entry) {
        Account account = entry.getAccount();
        String accountNumber = account.getAccountNumber();
        boolean isDebit = entry.isDebit();

        StringBuilder interpretation = new StringBuilder();

        // Interprétation selon le numéro de compte
        if (accountNumber.startsWith("411")) { // Clients
            if (isDebit) {
                interpretation.append("Augmentation de créance client (facturation ou LTA)");
            } else {
                interpretation.append("Diminution de créance client (encaissement)");
            }
        } else if (accountNumber.startsWith("531")) { // Caisse
            if (isDebit) {
                interpretation.append("Entrée d'espèces en caisse");
            } else {
                interpretation.append("Sortie d'espèces de la caisse");
            }
        } else if (accountNumber.startsWith("512")) { // Banque
            if (isDebit) {
                interpretation.append("Crédit bancaire (virement reçu)");
            } else {
                interpretation.append("Débit bancaire (virement émis)");
            }
        } else if (accountNumber.startsWith("701")) { // Ventes
            if (isDebit) {
                interpretation.append("Diminution du chiffre d'affaires (avoir)");
            } else {
                interpretation.append("Augmentation du chiffre d'affaires (vente)");
            }
        } else {
            interpretation.append("Mouvement sur compte ").append(account.getAccountName());
        }

        // Ajout du contexte selon la source
        JournalEntry journalEntry = entry.getJournalEntry();
        if (journalEntry.getSourceType() == SourceType.LTA_PAYMENT) {
            interpretation.append(" - Paiement LTA");
        } else if (journalEntry.getSourceType() == SourceType.INVOICE) {
            interpretation.append(" - Facturation");
        }

        return interpretation.toString();
    }

    /**
     * Valide l'équilibre comptable d'une période
     */
    public Map<String, Object> validateAccountingBalance(LocalDate startDate, LocalDate endDate) {
        List<JournalEntry> entries = getEntriesByDateRange(startDate, endDate);

        BigDecimal totalDebit = entries.stream()
                .map(JournalEntry::getTotalDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredit = entries.stream()
                .map(JournalEntry::getTotalCredit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean isBalanced = totalDebit.compareTo(totalCredit) == 0;
        BigDecimal difference = totalDebit.subtract(totalCredit);

        Map<String, Object> validation = new HashMap<>();
        validation.put("period", startDate + " au " + endDate);
        validation.put("totalDebit", totalDebit);
        validation.put("totalCredit", totalCredit);
        validation.put("isBalanced", isBalanced);
        validation.put("difference", difference);
        validation.put("entriesCount", entries.size());

        return validation;
    }

    // Méthodes utilitaires privées

    private String extractPaymentMethod(String description) {
        if (description.contains("CASH") || description.contains("ESPECES")) {
            return "ESPECES";
        } else if (description.contains("VIREMENT") || description.contains("BANK")) {
            return "VIREMENT";
        } else if (description.contains("CHEQUE")) {
            return "CHEQUE";
        }
        return "AUTRE";
    }

    private List<AccountingEntry> getRecentAccountMovements(String accountNumber, int limit) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            return List.of();
        }
        return accountingEntryRepository
                .findTop10ByAccountOrderByJournalEntry_EntryDateDesc(account);
    }
}
