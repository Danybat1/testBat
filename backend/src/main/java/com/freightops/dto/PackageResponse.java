package com.freightops.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Package entity responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageResponse {

    private Long id;
    private BigDecimal weight;
    private String description;
    private String trackingNumber;
    private String dimensions;
    private String notes;
    private Integer packageSequence;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Display format for lists
    public String getDisplayName() {
        return "Package " + packageSequence + " - " + description + " (" + weight + "kg)";
    }
}
