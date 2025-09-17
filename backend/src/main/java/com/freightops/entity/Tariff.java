package com.freightops.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tariff entity representing pricing between origin and destination cities
 * Used for automatic cost calculation in LTAs
 */
@Entity
@Table(name = "tariffs", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"origin_city_id", "destination_city_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tariff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Origin city is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_city_id", nullable = false)
    private City originCity;

    @NotNull(message = "Destination city is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_city_id", nullable = false)
    private City destinationCity;

    @NotNull(message = "Rate per kilogram is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rate per kilogram must be greater than 0")
    @Column(name = "kg_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal kgRate;

    // Volume coefficients for future calculations
    @DecimalMin(value = "0.0", message = "Volume coefficient v1 must be non-negative")
    @Column(name = "volume_coeff_v1", precision = 8, scale = 4)
    private BigDecimal volumeCoeffV1 = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Volume coefficient v2 must be non-negative")
    @Column(name = "volume_coeff_v2", precision = 8, scale = 4)
    private BigDecimal volumeCoeffV2 = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Volume coefficient v3 must be non-negative")
    @Column(name = "volume_coeff_v3", precision = 8, scale = 4)
    private BigDecimal volumeCoeffV3 = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_until")
    private LocalDateTime effectiveUntil;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor for quick creation
    public Tariff(City originCity, City destinationCity, BigDecimal kgRate) {
        this.originCity = originCity;
        this.destinationCity = destinationCity;
        this.kgRate = kgRate;
        this.isActive = true;
        this.effectiveFrom = LocalDateTime.now();
    }

    /**
     * Calculate cost for given weight
     */
    public BigDecimal calculateCost(BigDecimal weight) {
        if (weight == null || kgRate == null) {
            return BigDecimal.ZERO;
        }
        return weight.multiply(kgRate);
    }

    @Override
    public String toString() {
        return originCity.getIataCode() + " → " + destinationCity.getIataCode() + 
               " (" + kgRate + "/kg)";
    }
}
