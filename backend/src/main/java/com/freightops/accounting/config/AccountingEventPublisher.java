package com.freightops.accounting.config;

import com.freightops.accounting.events.InvoiceCreatedEvent;
import com.freightops.accounting.events.PaymentReceivedEvent;
import com.freightops.accounting.events.LTACompletedEvent;
import com.freightops.entity.Invoice;
import com.freightops.entity.Payment;
import com.freightops.entity.LTA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Composant pour publier les événements comptables
 * Utilisé par les services existants pour déclencher les écritures automatiques
 */
@Component
public class AccountingEventPublisher {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Publie un événement de création de facture
     * 
     * @param invoice   la facture créée
     * @param createdBy l'utilisateur qui a créé la facture
     */
    public void publishInvoiceCreatedEvent(Invoice invoice, String createdBy) {
        InvoiceCreatedEvent event = new InvoiceCreatedEvent(this, invoice, createdBy);
        eventPublisher.publishEvent(event);
    }

    /**
     * Publie un événement de réception de paiement
     * 
     * @param payment   le paiement reçu
     * @param createdBy l'utilisateur qui a enregistré le paiement
     */
    public void publishPaymentReceivedEvent(Payment payment, String createdBy) {
        PaymentReceivedEvent event = new PaymentReceivedEvent(this, payment, createdBy);
        eventPublisher.publishEvent(event);
    }

    /**
     * Publie un événement de finalisation de LTA
     * 
     * @param lta       la LTA finalisée
     * @param createdBy l'utilisateur qui a finalisé la LTA
     */
    public void publishLTACompletedEvent(LTA lta, String createdBy) {
        LTACompletedEvent event = new LTACompletedEvent(this, lta, createdBy);
        eventPublisher.publishEvent(event);
    }
}
