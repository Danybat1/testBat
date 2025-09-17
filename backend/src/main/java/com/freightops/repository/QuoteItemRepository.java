package com.freightops.repository;

import com.freightops.entity.QuoteItem;
import com.freightops.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuoteItemRepository extends JpaRepository<QuoteItem, Long> {
    
    List<QuoteItem> findByQuote(Quote quote);
    
    @Query("SELECT qi FROM QuoteItem qi WHERE qi.quote.id = :quoteId ORDER BY qi.id")
    List<QuoteItem> findByQuoteIdOrderById(@Param("quoteId") Long quoteId);
    
    void deleteByQuote(Quote quote);
}
