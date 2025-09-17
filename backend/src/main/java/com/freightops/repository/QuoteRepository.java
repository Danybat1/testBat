package com.freightops.repository;

import com.freightops.entity.Quote;
import com.freightops.entity.Client;
import com.freightops.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
    
    Optional<Quote> findByQuoteNumber(String quoteNumber);
    
    List<Quote> findByClient(Client client);
    
    List<Quote> findByStatus(InvoiceStatus status);
    
    List<Quote> findByConvertedFalse();
    
    @Query("SELECT q FROM Quote q WHERE q.validUntil < :date AND q.converted = false AND q.status != 'CANCELLED'")
    List<Quote> findExpiredQuotes(@Param("date") LocalDate date);
    
    @Query("SELECT q FROM Quote q WHERE q.client.id = :clientId ORDER BY q.quoteDate DESC")
    List<Quote> findByClientIdOrderByQuoteDateDesc(@Param("clientId") Long clientId);
    
    @Query("SELECT q FROM Quote q WHERE q.quoteDate BETWEEN :startDate AND :endDate ORDER BY q.quoteDate DESC")
    List<Quote> findByQuoteDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    boolean existsByQuoteNumber(String quoteNumber);
    
    @Query("SELECT COUNT(q) FROM Quote q WHERE YEAR(q.quoteDate) = :year")
    Long countByYear(@Param("year") int year);
}
