package com.freightops.enums;

public enum ServiceType {
    STANDARD("Standard"),
    EXPRESS("Express"),
    OVERNIGHT("Livraison le lendemain"),
    SAME_DAY("Livraison le jour même"),
    ECONOMY("Économique"),
    PRIORITY("Prioritaire");

    private final String label;

    ServiceType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public int getMaxDeliveryDays() {
        switch (this) {
            case SAME_DAY:
                return 1;
            case OVERNIGHT:
                return 1;
            case EXPRESS:
                return 2;
            case PRIORITY:
                return 3;
            case STANDARD:
                return 5;
            case ECONOMY:
                return 7;
            default:
                return 5;
        }
    }

    @Override
    public String toString() {
        return label;
    }
}
