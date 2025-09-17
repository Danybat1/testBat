package com.freightops.entity;

import com.freightops.enums.LTAStatus;
import com.freightops.enums.PaymentMode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
import java.util.UUID;

/**
 * LTA (Lettre de Transport Aérien) entity representing air freight shipments
 * Core business entity with origin/destination, payment mode, and package
 * management
 */
@Entity
@Table(name = "ltas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class LTA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lta_number", unique = true, length = 50)
    private String ltaNumber;

    @Column(name = "tracking_number", unique = true, length = 50)
    private String trackingNumber;

    @Column(name = "qr_code", length = 500)
    private String qrCode;

    @NotNull(message = "Origin city is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_city_id", nullable = false)
    private City originCity;

    @NotNull(message = "Destination city is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_city_id", nullable = false)
    private City destinationCity;

    @NotNull(message = "Payment mode is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false)
    private PaymentMode paymentMode;

    // Client is required only when payment mode is TO_INVOICE
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @NotNull(message = "Total weight is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total weight must be greater than 0")
    @Column(name = "total_weight", nullable = false, precision = 10, scale = 3)
    private BigDecimal totalWeight;

    @NotBlank(message = "Package nature is required")
    @Size(max = 200, message = "Package nature must not exceed 200 characters")
    @Column(name = "package_nature", nullable = false, length = 200)
    private String packageNature;

    @NotNull(message = "Package count is required")
    @Min(value = 1, message = "Package count must be at least 1")
    @Column(name = "package_count", nullable = false)
    private Integer packageCount;

    @Column(name = "calculated_cost", precision = 12, scale = 2)
    private BigDecimal calculatedCost;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LTAStatus status = LTAStatus.DRAFT;

    @Size(max = 200, message = "Shipper name must not exceed 200 characters")
    @Column(name = "shipper_name", length = 200)
    private String shipperName;

    @Size(max = 500, message = "Shipper address must not exceed 500 characters")
    @Column(name = "shipper_address", length = 500)
    private String shipperAddress;

    @Size(max = 200, message = "Consignee name must not exceed 200 characters")
    @Column(name = "consignee_name", length = 200)
    private String consigneeName;

    @Size(max = 500, message = "Consignee address must not exceed 500 characters")
    @Column(name = "consignee_address", length = 500)
    private String consigneeAddress;

    @Size(max = 1000, message = "Special instructions must not exceed 1000 characters")
    @Column(name = "special_instructions", length = 1000)
    private String specialInstructions;

    @Column(name = "declared_value", precision = 12, scale = 2)
    private BigDecimal declaredValue;

    @Column(name = "pickup_date")
    private LocalDateTime pickupDate;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // One-to-many relationship with packages
    @OneToMany(mappedBy = "lta", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Package> packages = new ArrayList<>();

    // Constructor for quick creation
    public LTA(City originCity, City destinationCity, PaymentMode paymentMode,
            BigDecimal totalWeight, String packageNature, Integer packageCount) {
        this.originCity = originCity;
        this.destinationCity = destinationCity;
        this.paymentMode = paymentMode;
        this.totalWeight = totalWeight;
        this.packageNature = packageNature;
        this.packageCount = packageCount;
        this.status = LTAStatus.DRAFT;
    }

    /**
     * Generate LTA number and tracking number if not set
     */
    @PrePersist
    public void generateNumbers() {
        if (ltaNumber == null) {
            ltaNumber = "LTA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (trackingNumber == null) {
            trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-"
                    + String.format("%03d", (int) (Math.random() * 1000));
        }
    }

    /**
     * Business validation: Client is required when payment mode is TO_INVOICE
     */
    public boolean isClientRequired() {
        return PaymentMode.TO_INVOICE.equals(paymentMode);
    }

    /**
     * Validate business rules
     */
    public boolean isValid() {
        if (isClientRequired() && client == null) {
            return false;
        }
        return true;
    }

    /**
     * Calculate total weight from packages
     */
    public BigDecimal calculateTotalWeightFromPackages() {
        return packages.stream()
                .map(Package::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Add package to LTA
     */
    public void addPackage(Package pkg) {
        packages.add(pkg);
        pkg.setLta(this);
        pkg.setPackageSequence(packages.size());
    }

    /**
     * Remove package from LTA
     */
    public void removePackage(Package pkg) {
        packages.remove(pkg);
        pkg.setLta(null);
        // Resequence remaining packages
        for (int i = 0; i < packages.size(); i++) {
            packages.get(i).setPackageSequence(i + 1);
        }
    }

    @Override
    public String toString() {
        return ltaNumber + " (" + originCity.getIataCode() + " → " +
                destinationCity.getIataCode() + ")";
    }
}
