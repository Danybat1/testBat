package com.freightops.repository;

import com.freightops.entity.Payment;
import com.freightops.entity.Invoice;
import com.freightops.entity.CashBox;
import com.freightops.entity.BankAccount;
import com.freightops.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoice(Invoice invoice);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByCashBox(CashBox cashBox);

    List<Payment> findByBankAccount(BankAccount bankAccount);

    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate ORDER BY p.paymentDate DESC")
    List<Payment> findByPaymentDateBetween(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM Payment p WHERE p.invoice.id = :invoiceId ORDER BY p.paymentDate DESC")
    List<Payment> findByInvoiceIdOrderByPaymentDateDesc(@Param("invoiceId") Long invoiceId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal sumCompletedPaymentsByDateRange(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.cashBox.id = :cashBoxId AND p.status = 'COMPLETED' AND p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal sumCompletedPaymentsByCashBoxAndDateRange(@Param("cashBoxId") Long cashBoxId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.bankAccount.id = :bankAccountId AND p.status = 'COMPLETED' AND p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal sumCompletedPaymentsByBankAccountAndDateRange(@Param("bankAccountId") Long bankAccountId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // New methods for cash statement
    List<Payment> findByCashBoxAndPaymentDateBetweenOrderByPaymentDateAsc(CashBox cashBox, LocalDate startDate,
            LocalDate endDate);
}
