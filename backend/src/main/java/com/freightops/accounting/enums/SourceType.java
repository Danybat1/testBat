package com.freightops.accounting.enums;

/**
 * Types de sources pour les écritures comptables
 * Permet de tracer l'origine de chaque écriture automatique
 */
public enum SourceType {
    INVOICE("Facture", "Écriture générée lors de la création d'une facture"),
    PAYMENT("Paiement", "Écriture générée lors d'un encaissement"),
    LTA("LTA", "Écriture générée lors de la finalisation d'une LTA"),
    LTA_PAYMENT("Paiement LTA", "Écriture générée lors d'un encaissement LTA"),
    TREASURY("Trésorerie", "Écriture de mouvement de trésorerie"),
    MANUAL("Manuel", "Écriture saisie manuellement"),
    ADJUSTMENT("Ajustement", "Écriture d'ajustement ou de correction"),
    OPENING("Ouverture", "Écriture d'ouverture d'exercice"),
    CLOSING("Clôture", "Écriture de clôture d'exercice");

    private final String label;
    private final String description;

    SourceType(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Détermine si le type de source est automatique
     * 
     * @return true si l'écriture est générée automatiquement
     */
    public boolean isAutomatic() {
        return this != MANUAL;
    }
}
