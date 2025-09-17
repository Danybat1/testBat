package com.freightops.accounting.repository;

import com.freightops.accounting.entity.JournalEntry;
import com.freightops.accounting.enums.SourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository pour la gestion des écritures comptables
 */
@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    /**
     * Trouve une écriture par son numéro
     */
    JournalEntry findByEntryNumber(String entryNumber);

    /**
     * Trouve les écritures par exercice comptable
     */
    List<JournalEntry> findByFiscalYearIdOrderByEntryDateDesc(Long fiscalYearId);

    /**
     * Trouve les écritures par période
     */
    List<JournalEntry> findByEntryDateBetweenOrderByEntryDateDesc(LocalDate startDate, LocalDate endDate);

    /**
     * Trouve les écritures par période (méthode alternative)
     */
    List<JournalEntry> findByEntryDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Trouve les écritures par source
     */
    List<JournalEntry> findBySourceTypeAndSourceId(String sourceType, Long sourceId);

    /**
     * Trouve les écritures par type de source
     */
    List<JournalEntry> findBySourceType(SourceType sourceType);

    /**
     * Trouve les écritures par type de source et période
     */
    List<JournalEntry> findBySourceTypeAndEntryDateBetween(SourceType sourceType, LocalDate startDate,
            LocalDate endDate);

    /**
     * Trouve le dernier numéro d'écriture pour un exercice
     */
    @Query("SELECT je.entryNumber FROM JournalEntry je WHERE je.fiscalYear.id = :fiscalYearId ORDER BY je.entryNumber DESC LIMIT 1")
    String findLastEntryNumberByFiscalYear(@Param("fiscalYearId") Long fiscalYearId);

    /**
     * Trouve les écritures non équilibrées
     */
    List<JournalEntry> findByIsBalancedFalse();

    /**
     * Trouve les écritures automatiques
     */
    @Query("SELECT je FROM JournalEntry je WHERE je.sourceType IS NOT NULL AND je.sourceType != 'MANUAL' ORDER BY je.entryDate DESC")
    List<JournalEntry> findAutomaticEntries();

    /**
     * Trouve les écritures manuelles
     */
    @Query("SELECT je FROM JournalEntry je WHERE je.sourceType IS NULL OR je.sourceType = 'MANUAL' ORDER BY je.entryDate DESC")
    List<JournalEntry> findManualEntries();

    /**
     * Compte les écritures par exercice
     */
    long countByFiscalYearId(Long fiscalYearId);

    /**
     * Calcule le total des débits pour un exercice
     */
    @Query("SELECT COALESCE(SUM(je.totalDebit), 0) FROM JournalEntry je WHERE je.fiscalYear.id = :fiscalYearId")
    java.math.BigDecimal getTotalDebitByFiscalYear(@Param("fiscalYearId") Long fiscalYearId);

    /**
     * Calcule le total des crédits pour un exercice
     */
    @Query("SELECT COALESCE(SUM(je.totalCredit), 0) FROM JournalEntry je WHERE je.fiscalYear.id = :fiscalYearId")
    java.math.BigDecimal getTotalCreditByFiscalYear(@Param("fiscalYearId") Long fiscalYearId);
}
