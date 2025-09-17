package com.freightops.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for City entity responses
 */
@Data
@NoArgsConstructor
public class CityResponse {

    private Long id;
    private String name;
    private String iataCode;
    private String country;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Display format for dropdowns/autocomplete
    public String getDisplayName() {
        return name + " (" + iataCode + ")";
    }
}
