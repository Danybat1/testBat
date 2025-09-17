package com.freightops.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating and updating Package entities
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageRequest {

    @NotNull(message = "Package weight is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Package weight must be greater than 0")
    private BigDecimal weight;

    @NotBlank(message = "Package description is required")
    @Size(max = 500, message = "Package description must not exceed 500 characters")
    private String description;

    @Size(max = 100, message = "Package dimensions must not exceed 100 characters")
    private String dimensions;

    @Size(max = 200, message = "Package notes must not exceed 200 characters")
    private String notes;

    private Integer packageSequence;
}
