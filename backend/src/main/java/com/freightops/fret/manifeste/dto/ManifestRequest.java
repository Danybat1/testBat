package com.freightops.fret.manifeste.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ManifestRequest {

    // Basic information
    private String proformaNumber;
    private String transportMode;
    private String vehicleReference;
    private String driverName;
    private String driverPhone;
    private LocalDateTime scheduledDeparture;
    private LocalDateTime scheduledArrival;

    // Shipper information
    private String shipperName;
    private String shipperAddress;
    private String shipperContact;
    private String shipperPhone;

    // Consignee information
    private String consigneeName;
    private String consigneeAddress;
    private String consigneeContact;
    private String consigneePhone;

    // Client information
    private String clientName;
    private String clientReference;
    private String clientContact;
    private String clientPhone;

    // Agent information
    private String agentName;
    private String agentAddress;
    private String agentContact;
    private String agentPhone;

    // Instructions and remarks
    private String deliveryInstructions;
    private String generalRemarks;
    private String attachments;

    // Items
    private List<ManifestItemRequest> items;

    // Constructors
    public ManifestRequest() {
    }

    // Getters and Setters
    public String getProformaNumber() {
        return proformaNumber;
    }

    public void setProformaNumber(String proformaNumber) {
        this.proformaNumber = proformaNumber;
    }

    public String getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(String transportMode) {
        this.transportMode = transportMode;
    }

    public String getVehicleReference() {
        return vehicleReference;
    }

    public void setVehicleReference(String vehicleReference) {
        this.vehicleReference = vehicleReference;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }

    public LocalDateTime getScheduledDeparture() {
        return scheduledDeparture;
    }

    public void setScheduledDeparture(LocalDateTime scheduledDeparture) {
        this.scheduledDeparture = scheduledDeparture;
    }

    public LocalDateTime getScheduledArrival() {
        return scheduledArrival;
    }

    public void setScheduledArrival(LocalDateTime scheduledArrival) {
        this.scheduledArrival = scheduledArrival;
    }

    public String getShipperName() {
        return shipperName;
    }

    public void setShipperName(String shipperName) {
        this.shipperName = shipperName;
    }

    public String getShipperAddress() {
        return shipperAddress;
    }

    public void setShipperAddress(String shipperAddress) {
        this.shipperAddress = shipperAddress;
    }

    public String getShipperContact() {
        return shipperContact;
    }

    public void setShipperContact(String shipperContact) {
        this.shipperContact = shipperContact;
    }

    public String getShipperPhone() {
        return shipperPhone;
    }

    public void setShipperPhone(String shipperPhone) {
        this.shipperPhone = shipperPhone;
    }

    public String getConsigneeName() {
        return consigneeName;
    }

    public void setConsigneeName(String consigneeName) {
        this.consigneeName = consigneeName;
    }

    public String getConsigneeAddress() {
        return consigneeAddress;
    }

    public void setConsigneeAddress(String consigneeAddress) {
        this.consigneeAddress = consigneeAddress;
    }

    public String getConsigneeContact() {
        return consigneeContact;
    }

    public void setConsigneeContact(String consigneeContact) {
        this.consigneeContact = consigneeContact;
    }

    public String getConsigneePhone() {
        return consigneePhone;
    }

    public void setConsigneePhone(String consigneePhone) {
        this.consigneePhone = consigneePhone;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientReference() {
        return clientReference;
    }

    public void setClientReference(String clientReference) {
        this.clientReference = clientReference;
    }

    public String getClientContact() {
        return clientContact;
    }

    public void setClientContact(String clientContact) {
        this.clientContact = clientContact;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(String agentAddress) {
        this.agentAddress = agentAddress;
    }

    public String getAgentContact() {
        return agentContact;
    }

    public void setAgentContact(String agentContact) {
        this.agentContact = agentContact;
    }

    public String getAgentPhone() {
        return agentPhone;
    }

    public void setAgentPhone(String agentPhone) {
        this.agentPhone = agentPhone;
    }

    public String getDeliveryInstructions() {
        return deliveryInstructions;
    }

    public void setDeliveryInstructions(String deliveryInstructions) {
        this.deliveryInstructions = deliveryInstructions;
    }

    public String getGeneralRemarks() {
        return generalRemarks;
    }

    public void setGeneralRemarks(String generalRemarks) {
        this.generalRemarks = generalRemarks;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public List<ManifestItemRequest> getItems() {
        return items;
    }

    public void setItems(List<ManifestItemRequest> items) {
        this.items = items;
    }

    // Nested class for manifest items
    public static class ManifestItemRequest {
        private String trackingNumber;
        private String description;
        private String packagingType;
        private Integer packageCount;
        private BigDecimal grossWeight;
        private BigDecimal volume;
        private BigDecimal volumetricWeight;
        private BigDecimal declaredValue;
        private String containerNumber;
        private String remarks;

        // Constructors
        public ManifestItemRequest() {
        }

        // Getters and Setters
        public String getTrackingNumber() {
            return trackingNumber;
        }

        public void setTrackingNumber(String trackingNumber) {
            this.trackingNumber = trackingNumber;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPackagingType() {
            return packagingType;
        }

        public void setPackagingType(String packagingType) {
            this.packagingType = packagingType;
        }

        public Integer getPackageCount() {
            return packageCount;
        }

        public void setPackageCount(Integer packageCount) {
            this.packageCount = packageCount;
        }

        public BigDecimal getGrossWeight() {
            return grossWeight;
        }

        public void setGrossWeight(BigDecimal grossWeight) {
            this.grossWeight = grossWeight;
        }

        public BigDecimal getVolume() {
            return volume;
        }

        public void setVolume(BigDecimal volume) {
            this.volume = volume;
        }

        public BigDecimal getVolumetricWeight() {
            return volumetricWeight;
        }

        public void setVolumetricWeight(BigDecimal volumetricWeight) {
            this.volumetricWeight = volumetricWeight;
        }

        public BigDecimal getDeclaredValue() {
            return declaredValue;
        }

        public void setDeclaredValue(BigDecimal declaredValue) {
            this.declaredValue = declaredValue;
        }

        public String getContainerNumber() {
            return containerNumber;
        }

        public void setContainerNumber(String containerNumber) {
            this.containerNumber = containerNumber;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }
    }
}
