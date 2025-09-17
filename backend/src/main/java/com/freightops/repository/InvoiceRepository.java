package com.freightops.repository;

import com.freightops.entity.Invoice;
import com.freightops.entity.Client;
import com.freightops.entity.Quote;
import com.freightops.entity.LTA;
import com.freightops.enums.InvoiceType;
import com.freightops.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    List<Invoice> findByClient(Client client);
    
    List<Invoice> findByType(InvoiceType type);
    
    List<Invoice> findByStatus(InvoiceStatus status);
    
    Optional<Invoice> findByQuote(Quote quote);
    
    Optional<Invoice> findByLta(LTA lta);
    
    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :date AND i.status IN ('SENT', 'PARTIALLY_PAID')")
    List<Invoice> findOverdueInvoices(@Param("date") LocalDate date);
    
    @Query("SELECT i FROM Invoice i WHERE i.client.id = :clientId ORDER BY i.invoiceDate DESC")
    List<Invoice> findByClientIdOrderByInvoiceDateDesc(@Param("clientId") Long clientId);
    
    @Query("SELECT i FROM Invoice i WHERE i.invoiceDate BETWEEN :startDate AND :endDate ORDER BY i.invoiceDate DESC")
    List<Invoice> findByInvoiceDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT i FROM Invoice i WHERE i.type = :type AND i.invoiceDate BETWEEN :startDate AND :endDate")
    List<Invoice> findByTypeAndInvoiceDateBetween(@Param("type") InvoiceType type, 
                                                 @Param("startDate") LocalDate startDate, 
                                                 @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.status = 'PAID' AND i.invoiceDate BETWEEN :startDate AND :endDate")
    BigDecimal sumPaidAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(i.remainingAmount) FROM Invoice i WHERE i.status IN ('SENT', 'PARTIALLY_PAID', 'OVERDUE')")
    BigDecimal sumRemainingAmount();
    
    boolean existsByInvoiceNumber(String invoiceNumber);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE YEAR(i.invoiceDate) = :year")
    Long countByYear(@Param("year") int year);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE YEAR(i.invoiceDate) = :year AND MONTH(i.invoiceDate) = :month")
    Long countByYearAndMonth(@Param("year") int year, @Param("month") int month);
}
