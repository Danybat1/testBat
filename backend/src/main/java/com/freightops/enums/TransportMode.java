package com.freightops.enums;

/**
 * Transport mode values for Manifest
 */
public enum TransportMode {
    AIR("Aérien"),
    ROAD("Routier"),
    SEA("Maritime"),
    RAIL("Ferroviaire"),
    MULTIMODAL("Multimodal");

    private final String displayName;

    TransportMode(String displayName) {
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
