package com.freightops.dto;

import com.freightops.enums.LTAStatus;
import com.freightops.enums.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for LTA entity responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LTAResponse {

    private Long id;
    private String ltaNumber;
    private String trackingNumber;
    private CityResponse originCity;
    private CityResponse destinationCity;
    private PaymentMode paymentMode;
    private ClientResponse client;
    private BigDecimal totalWeight;
    private String packageNature;
    private Integer packageCount;
    private BigDecimal calculatedCost;
    private LTAStatus status;
    private String shipperName;
    private String shipperAddress;
    private String consigneeName;
    private String consigneeAddress;
    private String specialInstructions;
    private BigDecimal declaredValue;
    private LocalDateTime pickupDate;
    private LocalDateTime deliveryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PackageResponse> packages;
    private List<LTAStatusHistoryResponse> statusHistory;

    // Display format for lists
    public String getDisplayName() {
        return ltaNumber + " (" + originCity.getIataCode() + " → " +
                destinationCity.getIataCode() + ")";
    }

    // Status display with color coding
    public String getStatusColor() {
        return switch (status) {
            case DRAFT -> "warning";
            case CONFIRMED -> "info";
            case IN_TRANSIT -> "primary";
            case DELIVERED -> "success";
            case CANCELLED -> "danger";
        };
    }
}
