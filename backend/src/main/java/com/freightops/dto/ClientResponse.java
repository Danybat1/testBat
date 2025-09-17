package com.freightops.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Client entity responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {

    private Long id;
    private String name;
    private String address;
    private String contactNumber;
    private String email;
    private String contactPerson;
    private String notes;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer ltaCount; // Number of associated LTAs

    // Display format for dropdowns
    public String getDisplayName() {
        return name + (contactNumber != null ? " (" + contactNumber + ")" : "");
    }
}
