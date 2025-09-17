package com.freightops.accounting.events;

import com.freightops.entity.Payment;
import org.springframework.context.ApplicationEvent;

/**
 * Événement déclenché lors de la réception d'un paiement
 * Permet de générer automatiquement les écritures comptables d'encaissement
 */
public class PaymentReceivedEvent extends ApplicationEvent {

    private final Payment payment;
    private final String createdBy;

    public PaymentReceivedEvent(Object source, Payment payment, String createdBy) {
        super(source);
        this.payment = payment;
        this.createdBy = createdBy;
    }

    public Payment getPayment() {
        return payment;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}
