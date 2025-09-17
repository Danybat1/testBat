package com.freightops.enums;

/**
 * Status values for LTA lifecycle management
 */
public enum LTAStatus {
    DRAFT("Draft"),
    CONFIRMED("Confirmed"),
    IN_TRANSIT("In Transit"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String displayName;

    LTAStatus(String displayName) {
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
