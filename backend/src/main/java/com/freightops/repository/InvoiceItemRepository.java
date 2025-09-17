package com.freightops.repository;

import com.freightops.entity.InvoiceItem;
import com.freightops.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
    
    List<InvoiceItem> findByInvoice(Invoice invoice);
    
    @Query("SELECT ii FROM InvoiceItem ii WHERE ii.invoice.id = :invoiceId ORDER BY ii.id")
    List<InvoiceItem> findByInvoiceIdOrderById(@Param("invoiceId") Long invoiceId);
    
    void deleteByInvoice(Invoice invoice);
}
