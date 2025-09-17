package com.freightops.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for creating and updating Tariff entities
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TariffRequest {

    @NotNull(message = "Origin city ID is required")
    private Long originCityId;

    @NotNull(message = "Destination city ID is required")
    private Long destinationCityId;

    @NotNull(message = "Rate per kilogram is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rate per kilogram must be greater than 0")
    private BigDecimal kgRate;

    @DecimalMin(value = "0.0", message = "Volume coefficient v1 must be non-negative")
    private BigDecimal volumeCoeffV1 = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Volume coefficient v2 must be non-negative")
    private BigDecimal volumeCoeffV2 = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Volume coefficient v3 must be non-negative")
    private BigDecimal volumeCoeffV3 = BigDecimal.ZERO;

    private Boolean isActive = true;

    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveUntil;
}
