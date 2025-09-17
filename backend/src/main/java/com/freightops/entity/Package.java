package com.freightops.entity;

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

/**
 * Package entity representing individual packages within an LTA
 * Each package has its own weight, description, and tracking number
 */
@Entity
@Table(name = "packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Package {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "LTA is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lta_id", nullable = false)
    private LTA lta;

    @NotNull(message = "Package weight is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Package weight must be greater than 0")
    @Column(nullable = false, precision = 8, scale = 3)
    private BigDecimal weight;

    @NotBlank(message = "Package description is required")
    @Size(max = 500, message = "Package description must not exceed 500 characters")
    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "tracking_number", length = 50, unique = true)
    private String trackingNumber;

    @Size(max = 100, message = "Package dimensions must not exceed 100 characters")
    @Column(length = 100)
    private String dimensions;

    @Size(max = 200, message = "Package notes must not exceed 200 characters")
    @Column(length = 200)
    private String notes;

    @Column(name = "package_sequence")
    private Integer packageSequence;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor for quick creation
    public Package(LTA lta, BigDecimal weight, String description) {
        this.lta = lta;
        this.weight = weight;
        this.description = description;
    }

    /**
     * Generate tracking number if not set
     */
    @PrePersist
    public void generateTrackingNumber() {
        if (trackingNumber == null && lta != null) {
            trackingNumber = "PKG-" + lta.getId() + "-" + System.currentTimeMillis();
        }
    }

    @Override
    public String toString() {
        return "Package " + trackingNumber + " (" + weight + "kg)";
    }
}
