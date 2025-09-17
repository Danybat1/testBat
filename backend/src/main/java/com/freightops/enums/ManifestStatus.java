package com.freightops.enums;

/**
 * Status values for Manifest lifecycle management
 */
public enum ManifestStatus {
    DRAFT("Brouillon"),
    CONFIRMED("Confirmé"),
    IN_TRANSIT("En transit"),
    DELIVERED("Livré"),
    CANCELLED("Annulé");

    private final String displayName;

    ManifestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
