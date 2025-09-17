package com.freightops.enums;

public enum ShipmentStatus {
    PENDING("En attente"),
    CONFIRMED("Confirmé"),
    PICKUP_SCHEDULED("Enlèvement programmé"),
    PICKED_UP("Enlevé"),
    IN_TRANSIT("En transit"),
    OUT_FOR_DELIVERY("En cours de livraison"),
    DELIVERED("Livré"),
    CANCELLED("Annulé"),
    RETURNED("Retourné"),
    RETURNED_TO_SENDER("Retourné à l'expéditeur"),
    EXCEPTION("Exception"),
    LOST("Perdu"),
    DAMAGED("Endommagé"),
    CREATED("Créé");

    private final String label;

    ShipmentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
