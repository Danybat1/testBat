package com.freightops.entity;

import com.freightops.enums.ManifestStatus;
import com.freightops.enums.TransportMode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Manifest entity representing shipping manifests
 * Core business entity for manifest management with parties and goods
 */
@Entity
@Table(name = "manifests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Manifest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "manifest_number", unique = true, length = 50)
    private String manifestNumber;

    @Column(name = "tracking_number", unique = true, length = 50)
    private String trackingNumber;

    @Column(name = "qr_code_data", length = 500)
    private String qrCodeData;

    // General Information
    @NotBlank(message = "Proforma number is required")
    @Size(max = 100, message = "Proforma number must not exceed 100 characters")
    @Column(name = "proforma_number", nullable = false, length = 100)
    private String proformaNumber;

    @NotNull(message = "Transport mode is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "transport_mode", nullable = false)
    private TransportMode transportMode;

    @Size(max = 100, message = "Vehicle info must not exceed 100 characters")
    @Column(name = "vehicle_info", length = 100)
    private String vehicleInfo;

    @Size(max = 200, message = "Driver name must not exceed 200 characters")
    @Column(name = "driver_name", length = 200)
    private String driverName;

    @Column(name = "departure_date")
    private LocalDateTime departureDate;

    @Column(name = "arrival_date")
    private LocalDateTime arrivalDate;

    // Status and tracking
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ManifestStatus status = ManifestStatus.DRAFT;

    // Totals
    @DecimalMin(value = "0.0", message = "Total weight must be non-negative")
    @Column(name = "total_weight", precision = 10, scale = 3)
    private BigDecimal totalWeight = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Total volume must be non-negative")
    @Column(name = "total_volume", precision = 10, scale = 3)
    private BigDecimal totalVolume = BigDecimal.ZERO;

    @Column(name = "total_packages")
    private Integer totalPackages = 0;

    @DecimalMin(value = "0.0", message = "Total value must be non-negative")
    @Column(name = "total_value", precision = 12, scale = 2)
    private BigDecimal totalValue = BigDecimal.ZERO;

    // Instructions and remarks
    @Size(max = 1000, message = "Delivery instructions must not exceed 1000 characters")
    @Column(name = "delivery_instructions", length = 1000)
    private String deliveryInstructions;

    @Size(max = 1000, message = "Remarks must not exceed 1000 characters")
    @Column(name = "remarks", length = 1000)
    private String remarks;

    // Signatures
    @Column(name = "loading_signature_date")
    private LocalDateTime loadingSignatureDate;

    @Size(max = 500, message = "Loading signature remarks must not exceed 500 characters")
    @Column(name = "loading_signature_remarks", length = 500)
    private String loadingSignatureRemarks;

    @Column(name = "delivery_signature_date")
    private LocalDateTime deliverySignatureDate;

    @Size(max = 500, message = "Delivery signature remarks must not exceed 500 characters")
    @Column(name = "delivery_signature_remarks", length = 500)
    private String deliverySignatureRemarks;

    // Attachments
    @Size(max = 2000, message = "Attachments must not exceed 2000 characters")
    @Column(name = "attachments", length = 2000)
    private String attachments;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "manifest", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ManifestParty> parties = new ArrayList<>();

    @OneToMany(mappedBy = "manifest", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ManifestGoods> goods = new ArrayList<>();

    /**
     * Generate manifest number and tracking number if not set
     */
    @PrePersist
    public void generateNumbers() {
        if (manifestNumber == null) {
            manifestNumber = "MAN-" + System.currentTimeMillis();
        }
        if (trackingNumber == null) {
            trackingNumber = "TRK" + String.valueOf(System.currentTimeMillis()).substring(5) +
                    generateRandomString(4);
        }
        if (qrCodeData == null) {
            qrCodeData = "MANIFEST:" + manifestNumber + "|TRACKING:" + trackingNumber;
        }
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return result.toString();
    }

    /**
     * Add party to manifest
     */
    public void addParty(ManifestParty party) {
        parties.add(party);
        party.setManifest(this);
    }

    /**
     * Remove party from manifest
     */
    public void removeParty(ManifestParty party) {
        parties.remove(party);
        party.setManifest(null);
    }

    /**
     * Add goods to manifest
     */
    public void addGoods(ManifestGoods goodsItem) {
        goods.add(goodsItem);
        goodsItem.setManifest(this);
        goodsItem.setLineNumber(goods.size());
    }

    /**
     * Remove goods from manifest
     */
    public void removeGoods(ManifestGoods goodsItem) {
        goods.remove(goodsItem);
        goodsItem.setManifest(null);
        // Resequence remaining goods
        for (int i = 0; i < goods.size(); i++) {
            goods.get(i).setLineNumber(i + 1);
        }
    }

    /**
     * Calculate totals from goods
     */
    public void calculateTotals() {
        totalWeight = goods.stream()
                .map(ManifestGoods::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalVolume = goods.stream()
                .map(ManifestGoods::getVolume)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalPackages = goods.stream()
                .mapToInt(ManifestGoods::getPackageCount)
                .sum();

        totalValue = goods.stream()
                .map(ManifestGoods::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get party by type
     */
    public ManifestParty getPartyByType(String partyType) {
        return parties.stream()
                .filter(party -> partyType.equals(party.getPartyType()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return manifestNumber + " (" + status + ")";
    }
}
