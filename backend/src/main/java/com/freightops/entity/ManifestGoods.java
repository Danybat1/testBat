package com.freightops.entity;

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

/**
 * ManifestGoods entity representing goods/merchandise in a manifest
 */
@Entity
@Table(name = "manifest_goods")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ManifestGoods {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Manifest is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manifest_id", nullable = false)
    private Manifest manifest;

    @NotNull(message = "Line number is required")
    @Min(value = 1, message = "Line number must be at least 1")
    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    @Size(max = 50, message = "Tracking number must not exceed 50 characters")
    @Column(name = "tracking_number", length = 50)
    private String trackingNumber;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Size(max = 100, message = "Packaging must not exceed 100 characters")
    @Column(name = "packaging", length = 100)
    private String packaging;

    @NotNull(message = "Package count is required")
    @Min(value = 1, message = "Package count must be at least 1")
    @Column(name = "package_count", nullable = false)
    private Integer packageCount;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than 0")
    @Column(name = "weight", nullable = false, precision = 10, scale = 3)
    private BigDecimal weight;

    @NotNull(message = "Volume is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Volume must be greater than 0")
    @Column(name = "volume", nullable = false, precision = 10, scale = 3)
    private BigDecimal volume;

    @NotNull(message = "Value is required")
    @DecimalMin(value = "0.0", message = "Value must be non-negative")
    @Column(name = "goods_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    @Size(max = 10, message = "Currency must not exceed 10 characters")
    @Column(name = "currency", length = 10)
    private String currency = "XAF";

    @Size(max = 100, message = "Origin must not exceed 100 characters")
    @Column(name = "origin", length = 100)
    private String origin;

    @Size(max = 100, message = "Destination must not exceed 100 characters")
    @Column(name = "destination", length = 100)
    private String destination;

    @Size(max = 500, message = "Special instructions must not exceed 500 characters")
    @Column(name = "special_instructions", length = 500)
    private String specialInstructions;

    @Size(max = 100, message = "Handling code must not exceed 100 characters")
    @Column(name = "handling_code", length = 100)
    private String handlingCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors for convenience
    public ManifestGoods(Manifest manifest, Integer lineNumber, String description,
            String packaging, Integer packageCount, BigDecimal weight,
            BigDecimal volume, BigDecimal value) {
        this.manifest = manifest;
        this.lineNumber = lineNumber;
        this.description = description;
        this.packaging = packaging;
        this.packageCount = packageCount;
        this.weight = weight;
        this.volume = volume;
        this.value = value;
        this.currency = "XAF";
    }

    public ManifestGoods(String description, String packaging, Integer packageCount,
            BigDecimal weight, BigDecimal volume, BigDecimal value,
            String origin, String destination) {
        this.description = description;
        this.packaging = packaging;
        this.packageCount = packageCount;
        this.weight = weight;
        this.volume = volume;
        this.value = value;
        this.origin = origin;
        this.destination = destination;
        this.currency = "XAF";
    }

    /**
     * Calculate weight per package
     */
    public BigDecimal getWeightPerPackage() {
        if (packageCount == null || packageCount == 0) {
            return BigDecimal.ZERO;
        }
        return weight.divide(BigDecimal.valueOf(packageCount), 3, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculate volume per package
     */
    public BigDecimal getVolumePerPackage() {
        if (packageCount == null || packageCount == 0) {
            return BigDecimal.ZERO;
        }
        return volume.divide(BigDecimal.valueOf(packageCount), 3, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculate value per package
     */
    public BigDecimal getValuePerPackage() {
        if (packageCount == null || packageCount == 0) {
            return BigDecimal.ZERO;
        }
        return value.divide(BigDecimal.valueOf(packageCount), 2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public String toString() {
        return "Line " + lineNumber + ": " + description + " (" + packageCount + " packages)";
    }
}
