package com.freightops.fret.manifeste.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "freight_manifests")
public class FreightManifest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "manifest_number", unique = true, nullable = false)
    private String manifestNumber;

    @Column(name = "proforma_number")
    private String proformaNumber;

    @Column(name = "transport_mode")
    private String transportMode;

    @Column(name = "vehicle_reference")
    private String vehicleReference;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "driver_phone")
    private String driverPhone;

    @Column(name = "scheduled_departure")
    private LocalDateTime scheduledDeparture;

    @Column(name = "scheduled_arrival")
    private LocalDateTime scheduledArrival;

    // Shipper information
    @Column(name = "shipper_name")
    private String shipperName;

    @Column(name = "shipper_address", length = 500)
    private String shipperAddress;

    @Column(name = "shipper_contact")
    private String shipperContact;

    @Column(name = "shipper_phone")
    private String shipperPhone;

    // Consignee information
    @Column(name = "consignee_name")
    private String consigneeName;

    @Column(name = "consignee_address", length = 500)
    private String consigneeAddress;

    @Column(name = "consignee_contact")
    private String consigneeContact;

    @Column(name = "consignee_phone")
    private String consigneePhone;

    // Client information
    @Column(name = "client_name")
    private String clientName;

    @Column(name = "client_reference")
    private String clientReference;

    @Column(name = "client_contact")
    private String clientContact;

    @Column(name = "client_phone")
    private String clientPhone;

    // Agent information
    @Column(name = "agent_name")
    private String agentName;

    @Column(name = "agent_address", length = 500)
    private String agentAddress;

    @Column(name = "agent_contact")
    private String agentContact;

    @Column(name = "agent_phone")
    private String agentPhone;

    @Column(name = "delivery_instructions", length = 1000)
    private String deliveryInstructions;

    @Column(name = "general_remarks", length = 1000)
    private String generalRemarks;

    @Column(name = "attachments", length = 1000)
    private String attachments; // JSON string of attachment list

    // Signature information
    @Column(name = "loading_signature", length = 1000)
    private String loadingSignature;

    @Column(name = "loading_signatory")
    private String loadingSignatory;

    @Column(name = "loading_signature_date")
    private LocalDateTime loadingSignatureDate;

    @Column(name = "delivery_signature", length = 1000)
    private String deliverySignature;

    @Column(name = "delivery_signatory")
    private String deliverySignatory;

    @Column(name = "delivery_signature_date")
    private LocalDateTime deliverySignatureDate;

    @Column(name = "delivery_remarks", length = 500)
    private String deliveryRemarks;

    // Totals
    @Column(name = "total_packages")
    private Integer totalPackages;

    @Column(name = "total_weight", precision = 10, scale = 2)
    private BigDecimal totalWeight;

    @Column(name = "total_volume", precision = 10, scale = 3)
    private BigDecimal totalVolume;

    @Column(name = "total_volumetric_weight", precision = 10, scale = 2)
    private BigDecimal totalVolumetricWeight;

    @Column(name = "total_value", precision = 15, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "qr_code", length = 1000)
    private String qrCode;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "manifest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ManifestItem> items;

    public FreightManifest() {
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

    public List<ManifestItem> getItems() {
        return items;
    }

    public void setItems(List<ManifestItem> items) {
        this.items = items;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
