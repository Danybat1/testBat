package com.freightops.enums;

/**
 * Payment modes for LTA shipments
 */
public enum PaymentMode {
    CASH("Cash Payment"),
    TO_INVOICE("To Invoice"),
    FREIGHT_COLLECT("Freight Collect"),
    FREE("Free of Charge");

    private final String displayName;

    PaymentMode(String displayName) {
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
