package com.freightops.accounting.service;

import com.freightops.accounting.entity.JournalEntry;
import com.freightops.accounting.entity.FiscalYear;
import com.freightops.accounting.repository.JournalEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service pour la gestion des écritures comptables
 * Gère la numérotation automatique et la validation des écritures
 */
@Service
@Transactional
public class JournalEntryService {

    private static final Logger logger = Logger.getLogger(JournalEntryService.class.getName());

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private FiscalYearService fiscalYearService;

    /**
     * Sauvegarde une écriture comptable avec numérotation automatique
     * 
     * @param journalEntry l'écriture à sauvegarder
     * @return l'écriture sauvegardée
     */
    public JournalEntry saveJournalEntry(JournalEntry journalEntry) {
        // Génération du numéro d'écriture si nécessaire
        if (journalEntry.getEntryNumber() == null || journalEntry.getEntryNumber().contains("TEMP")) {
            String entryNumber = generateEntryNumber(journalEntry.getFiscalYear());
            journalEntry.setEntryNumber(entryNumber);
        }

        // Validation de l'équilibre
        journalEntry.calculateTotals();
        journalEntry.validateBalance();

        if (!journalEntry.getIsBalanced()) {
            throw new IllegalStateException("L'écriture comptable n'est pas équilibrée (débit ≠ crédit)");
        }

        // Mise à jour des soldes des comptes
        journalEntry.getAccountingEntries().forEach(entry -> {
            entry.updateAccountBalance();
        });

        return journalEntryRepository.save(journalEntry);
    }

    /**
     * Génère automatiquement le numéro d'écriture
     * Format: JE-YYYY-NNNNNN
     */
    private String generateEntryNumber(FiscalYear fiscalYear) {
        String year = String.valueOf(fiscalYear.getYearNumber());

        // Récupération du dernier numéro pour l'exercice
        String lastEntryNumber = journalEntryRepository.findLastEntryNumberByFiscalYear(fiscalYear.getId());

        int nextSequence = 1;
        if (lastEntryNumber != null && lastEntryNumber.startsWith("JE-" + year + "-")) {
            try {
                String sequencePart = lastEntryNumber.substring(lastEntryNumber.lastIndexOf("-") + 1);
                nextSequence = Integer.parseInt(sequencePart) + 1;
            } catch (NumberFormatException e) {
                logger.warning("Erreur lors du parsing du numéro d'écriture: " + lastEntryNumber);
            }
        }

        return String.format("JE-%s-%06d", year, nextSequence);
    }

    /**
     * Récupère toutes les écritures d'un exercice comptable
     */
    public List<JournalEntry> getJournalEntriesByFiscalYear(Long fiscalYearId) {
        return journalEntryRepository.findByFiscalYearIdOrderByEntryDateDesc(fiscalYearId);
    }

    /**
     * Récupère les écritures par période
     */
    public List<JournalEntry> getJournalEntriesByDateRange(LocalDate startDate, LocalDate endDate) {
        return journalEntryRepository.findByEntryDateBetweenOrderByEntryDateDesc(startDate, endDate);
    }

    /**
     * Récupère une écriture par son numéro
     */
    public JournalEntry getJournalEntryByNumber(String entryNumber) {
        return journalEntryRepository.findByEntryNumber(entryNumber);
    }

    /**
     * Récupère les écritures liées à une source spécifique
     */
    public List<JournalEntry> getJournalEntriesBySource(String sourceType, Long sourceId) {
        return journalEntryRepository.findBySourceTypeAndSourceId(sourceType, sourceId);
    }
}
