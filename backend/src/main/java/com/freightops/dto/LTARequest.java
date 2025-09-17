package com.freightops.dto;

import com.freightops.enums.LTAStatus;
import com.freightops.enums.PaymentMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for creating and updating LTA entities
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LTARequest {

    @NotNull(message = "Origin city ID is required")
    private Long originCityId;

    @NotNull(message = "Destination city ID is required")
    private Long destinationCityId;

    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;

    // Required only when paymentMode is TO_INVOICE
    private Long clientId;

    @NotNull(message = "Total weight is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total weight must be greater than 0")
    private BigDecimal totalWeight;

    @NotBlank(message = "Package nature is required")
    @Size(max = 200, message = "Package nature must not exceed 200 characters")
    private String packageNature;

    @NotNull(message = "Package count is required")
    @Min(value = 1, message = "Package count must be at least 1")
    private Integer packageCount;

    private LTAStatus status = LTAStatus.DRAFT;

    @Size(max = 200, message = "Shipper name must not exceed 200 characters")
    private String shipperName;

    @Size(max = 500, message = "Shipper address must not exceed 500 characters")
    private String shipperAddress;

    @Size(max = 200, message = "Consignee name must not exceed 200 characters")
    private String consigneeName;

    @Size(max = 500, message = "Consignee address must not exceed 500 characters")
    private String consigneeAddress;

    @Size(max = 1000, message = "Special instructions must not exceed 1000 characters")
    private String specialInstructions;

    @DecimalMin(value = "0.0", message = "Declared value must be non-negative")
    private BigDecimal declaredValue;

    private LocalDateTime pickupDate;

    private LocalDateTime deliveryDate;

    @Valid
    private List<PackageRequest> packages;

    /**
     * Validate business rules
     */
    public boolean isValid() {
        // Client is required when payment mode is TO_INVOICE
        if (PaymentMode.TO_INVOICE.equals(paymentMode) && clientId == null) {
            return false;
        }
        return true;
    }
}
