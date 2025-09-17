package com.freightops.accounting.repository;

import com.freightops.accounting.entity.AccountingEntry;
import com.freightops.accounting.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository pour la gestion des lignes d'écritures comptables
 */
@Repository
public interface AccountingEntryRepository extends JpaRepository<AccountingEntry, Long> {

        /**
         * Trouve les lignes d'écriture par écriture comptable
         */
        List<AccountingEntry> findByJournalEntryIdOrderByLineOrder(Long journalEntryId);

        /**
         * Trouve les lignes d'écriture par compte
         */
        List<AccountingEntry> findByAccountIdOrderByJournalEntryEntryDateDesc(Long accountId);

        /**
         * Trouve les lignes d'écriture par compte (méthode alternative)
         */
        List<AccountingEntry> findByAccountOrderByJournalEntry_EntryDateDesc(Account account);

        /**
         * Trouve les lignes d'écriture par compte avec limite
         */
        List<AccountingEntry> findTop10ByAccountOrderByJournalEntry_EntryDateDesc(Account account);

        /**
         * Trouve les lignes d'écriture par compte et date
         */
        List<AccountingEntry> findByAccountAndJournalEntry_EntryDateLessThanEqual(Account account, LocalDate date);

        /**
         * Trouve les lignes d'écriture par compte et période
         */
        @Query("SELECT ae FROM AccountingEntry ae WHERE ae.account.id = :accountId " +
                        "AND ae.journalEntry.entryDate BETWEEN :startDate AND :endDate " +
                        "ORDER BY ae.journalEntry.entryDate DESC")
        List<AccountingEntry> findByAccountAndDateRange(@Param("accountId") Long accountId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        /**
         * Calcule le solde d'un compte à une date donnée
         */
        @Query("SELECT COALESCE(SUM(ae.debitAmount - ae.creditAmount), 0) FROM AccountingEntry ae " +
                        "WHERE ae.account.id = :accountId AND ae.journalEntry.entryDate <= :date")
        BigDecimal getAccountBalanceAtDate(@Param("accountId") Long accountId, @Param("date") LocalDate date);

        /**
         * Calcule le total des débits d'un compte
         */
        @Query("SELECT COALESCE(SUM(ae.debitAmount), 0) FROM AccountingEntry ae WHERE ae.account.id = :accountId")
        BigDecimal getTotalDebitByAccount(@Param("accountId") Long accountId);

        /**
         * Calcule le total des crédits d'un compte
         */
        @Query("SELECT COALESCE(SUM(ae.creditAmount), 0) FROM AccountingEntry ae WHERE ae.account.id = :accountId")
        BigDecimal getTotalCreditByAccount(@Param("accountId") Long accountId);

        /**
         * Trouve les lignes d'écriture invalides (débit ET crédit > 0)
         */
        @Query("SELECT ae FROM AccountingEntry ae WHERE ae.debitAmount > 0 AND ae.creditAmount > 0")
        List<AccountingEntry> findInvalidEntries();

        /**
         * Trouve les lignes d'écriture par numéro de compte
         */
        @Query("SELECT ae FROM AccountingEntry ae WHERE ae.account.accountNumber = :accountNumber " +
                        "ORDER BY ae.journalEntry.entryDate DESC")
        List<AccountingEntry> findByAccountNumber(@Param("accountNumber") String accountNumber);

        /**
         * Trouve les mouvements d'un compte par exercice comptable
         */
        @Query("SELECT ae FROM AccountingEntry ae WHERE ae.account.id = :accountId " +
                        "AND ae.journalEntry.fiscalYear.id = :fiscalYearId " +
                        "ORDER BY ae.journalEntry.entryDate DESC")
        List<AccountingEntry> findByAccountAndFiscalYear(@Param("accountId") Long accountId,
                        @Param("fiscalYearId") Long fiscalYearId);
}
