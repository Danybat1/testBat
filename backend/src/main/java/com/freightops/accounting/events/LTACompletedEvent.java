package com.freightops.accounting.events;

import com.freightops.entity.LTA;
import org.springframework.context.ApplicationEvent;

/**
 * Événement déclenché lors de la finalisation d'une LTA
 * Permet de générer automatiquement les écritures comptables de revenus
 */
public class LTACompletedEvent extends ApplicationEvent {

    private final LTA lta;
    private final String createdBy;

    public LTACompletedEvent(Object source, LTA lta, String createdBy) {
        super(source);
        this.lta = lta;
        this.createdBy = createdBy;
    }

    public LTA getLta() {
        return lta;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}
