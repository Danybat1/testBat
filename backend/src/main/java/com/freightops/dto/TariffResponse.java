package com.freightops.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Tariff entity responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TariffResponse {

    private Long id;
    private CityResponse originCity;
    private CityResponse destinationCity;
    private BigDecimal kgRate;
    private BigDecimal volumeCoeffV1;
    private BigDecimal volumeCoeffV2;
    private BigDecimal volumeCoeffV3;
    private Boolean isActive;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Display format for lists
    public String getDisplayName() {
        return originCity.getIataCode() + " → " + destinationCity.getIataCode() + 
               " (" + kgRate + "/kg)";
    }

    // Calculate cost for given weight
    public BigDecimal calculateCost(BigDecimal weight) {
        if (weight == null || kgRate == null) {
            return BigDecimal.ZERO;
        }
        return weight.multiply(kgRate);
    }
}
