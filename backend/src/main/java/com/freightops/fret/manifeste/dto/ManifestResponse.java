package com.freightops.fret.manifeste.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ManifestResponse {

    private Long id;
    private String manifestNumber;
    private String message;

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

    // Totals
    private Integer totalPackages;
    private BigDecimal totalWeight;
    private BigDecimal totalVolume;
    private BigDecimal totalVolumetricWeight;
    private BigDecimal totalValue;

    // Signatures
    private String loadingSignature;
    private String loadingSignatory;
    private LocalDateTime loadingSignatureDate;
    private String deliverySignature;
    private String deliverySignatory;
    private LocalDateTime deliverySignatureDate;
    private String deliveryRemarks;

    // Metadata
    private String qrCode;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Items
    private List<ManifestItemResponse> items;

    // Constructors
    public ManifestResponse() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getManifestNumber() {
        return manifestNumber;
    }

    public void setManifestNumber(String manifestNumber) {
        this.manifestNumber = manifestNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

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

    public Integer getTotalPackages() {
        return totalPackages;
    }

    public void setTotalPackages(Integer totalPackages) {
        this.totalPackages = totalPackages;
    }

    public BigDecimal getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(BigDecimal totalWeight) {
        this.totalWeight = totalWeight;
    }

    public BigDecimal getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(BigDecimal totalVolume) {
        this.totalVolume = totalVolume;
    }

    public BigDecimal getTotalVolumetricWeight() {
        return totalVolumetricWeight;
    }

    public void setTotalVolumetricWeight(BigDecimal totalVolumetricWeight) {
        this.totalVolumetricWeight = totalVolumetricWeight;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public String getLoadingSignature() {
        return loadingSignature;
    }

    public void setLoadingSignature(String loadingSignature) {
        this.loadingSignature = loadingSignature;
    }

    public String getLoadingSignatory() {
        return loadingSignatory;
    }

    public void setLoadingSignatory(String loadingSignatory) {
        this.loadingSignatory = loadingSignatory;
    }

    public LocalDateTime getLoadingSignatureDate() {
        return loadingSignatureDate;
    }

    public void setLoadingSignatureDate(LocalDateTime loadingSignatureDate) {
        this.loadingSignatureDate = loadingSignatureDate;
    }

    public String getDeliverySignature() {
        return deliverySignature;
    }

    public void setDeliverySignature(String deliverySignature) {
        this.deliverySignature = deliverySignature;
    }

    public String getDeliverySignatory() {
        return deliverySignatory;
    }

    public void setDeliverySignatory(String deliverySignatory) {
        this.deliverySignatory = deliverySignatory;
    }

    public LocalDateTime getDeliverySignatureDate() {
        return deliverySignatureDate;
    }

    public void setDeliverySignatureDate(LocalDateTime deliverySignatureDate) {
        this.deliverySignatureDate = deliverySignatureDate;
    }

    public String getDeliveryRemarks() {
        return deliveryRemarks;
    }

    public void setDeliveryRemarks(String deliveryRemarks) {
        this.deliveryRemarks = deliveryRemarks;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ManifestItemResponse> getItems() {
        return items;
    }

    public void setItems(List<ManifestItemResponse> items) {
        this.items = items;
    }

    // Nested class for manifest item responses
    public static class ManifestItemResponse {
        private Long id;
        private Integer lineNumber;
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
        public ManifestItemResponse() {
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Integer getLineNumber() {
            return lineNumber;
        }

        public void setLineNumber(Integer lineNumber) {
            this.lineNumber = lineNumber;
        }

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
