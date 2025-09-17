package com.freightops.dto;

import com.freightops.enums.LTAStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * LTA Data Transfer Object
 * Used for API requests and responses
 */
public class LTADto {

    private Long id;

    @NotBlank(message = "LTA number is required")
    private String ltaNumber;

    @NotNull(message = "Status is required")
    private LTAStatus status;

    @NotBlank(message = "Shipper is required")
    private String shipper;

    @NotBlank(message = "Consignee is required")
    private String consignee;

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private BigDecimal weight;

    @NotBlank(message = "Tracking number is required")
    private String trackingNumber;

    private String description;
    private String origin;
    private String destination;
    private BigDecimal declaredValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    // Constructors
    public LTADto() {}

    public LTADto(String ltaNumber, LTAStatus status, String shipper, String consignee,
                  BigDecimal weight, String trackingNumber) {
        this.ltaNumber = ltaNumber;
        this.status = status;
        this.shipper = shipper;
        this.consignee = consignee;
        this.weight = weight;
        this.trackingNumber = trackingNumber;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLtaNumber() {
        return ltaNumber;
    }

    public void setLtaNumber(String ltaNumber) {
        this.ltaNumber = ltaNumber;
    }

    public LTAStatus getStatus() {
        return status;
    }

    public void setStatus(LTAStatus status) {
        this.status = status;
    }

    public String getShipper() {
        return shipper;
    }

    public void setShipper(String shipper) {
        this.shipper = shipper;
    }

    public String getConsignee() {
        return consignee;
    }

    public void setConsignee(String consignee) {
        this.consignee = consignee;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public BigDecimal getDeclaredValue() {
        return declaredValue;
    }

    public void setDeclaredValue(BigDecimal declaredValue) {
        this.declaredValue = declaredValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
