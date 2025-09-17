package com.freightops.repository;

import com.freightops.entity.TreasuryTransaction;
import com.freightops.entity.CashBox;
import com.freightops.entity.BankAccount;
import com.freightops.entity.Payment;
import com.freightops.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TreasuryTransactionRepository extends JpaRepository<TreasuryTransaction, Long> {

    Optional<TreasuryTransaction> findByReference(String reference);

    List<TreasuryTransaction> findByType(TransactionType type);

    List<TreasuryTransaction> findByCashBox(CashBox cashBox);

    List<TreasuryTransaction> findByBankAccount(BankAccount bankAccount);

    List<TreasuryTransaction> findByPayment(Payment payment);

    @Query("SELECT t FROM TreasuryTransaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<TreasuryTransaction> findByTransactionDateBetween(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM TreasuryTransaction t WHERE t.cashBox.id = :cashBoxId ORDER BY t.transactionDate DESC")
    List<TreasuryTransaction> findByCashBoxIdOrderByTransactionDateDesc(@Param("cashBoxId") Long cashBoxId);

    @Query("SELECT t FROM TreasuryTransaction t WHERE t.bankAccount.id = :bankAccountId ORDER BY t.transactionDate DESC")
    List<TreasuryTransaction> findByBankAccountIdOrderByTransactionDateDesc(@Param("bankAccountId") Long bankAccountId);

    @Query("SELECT t FROM TreasuryTransaction t WHERE t.category = :category ORDER BY t.transactionDate DESC")
    List<TreasuryTransaction> findByCategoryOrderByTransactionDateDesc(@Param("category") String category);

    @Query("SELECT SUM(t.amount) FROM TreasuryTransaction t WHERE t.type = 'INCOME' AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumIncomeByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(t.amount) FROM TreasuryTransaction t WHERE t.type = 'EXPENSE' AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumExpenseByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(t.amount) FROM TreasuryTransaction t WHERE t.cashBox.id = :cashBoxId AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumByCashBoxAndTypeAndDateRange(@Param("cashBoxId") Long cashBoxId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(t.amount) FROM TreasuryTransaction t WHERE t.bankAccount.id = :bankAccountId AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumByBankAccountAndTypeAndDateRange(@Param("bankAccountId") Long bankAccountId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT t.category FROM TreasuryTransaction t WHERE t.category IS NOT NULL ORDER BY t.category")
    List<String> findDistinctCategories();

    boolean existsByReference(String reference);

    // New methods for cash statement
    List<TreasuryTransaction> findByCashBoxAndTransactionDateBeforeOrderByTransactionDateAsc(CashBox cashBox,
            LocalDate date);

    List<TreasuryTransaction> findByCashBoxAndTransactionDateBetweenOrderByTransactionDateAsc(CashBox cashBox,
            LocalDate startDate, LocalDate endDate);
}
