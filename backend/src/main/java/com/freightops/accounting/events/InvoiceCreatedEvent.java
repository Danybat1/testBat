package com.freightops.accounting.events;

import com.freightops.entity.Invoice;
import org.springframework.context.ApplicationEvent;

/**
 * Événement déclenché lors de la création d'une facture
 * Permet de générer automatiquement les écritures comptables
 */
public class InvoiceCreatedEvent extends ApplicationEvent {

    private final Invoice invoice;
    private final String createdBy;

    public InvoiceCreatedEvent(Object source, Invoice invoice, String createdBy) {
        super(source);
        this.invoice = invoice;
        this.createdBy = createdBy;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}
