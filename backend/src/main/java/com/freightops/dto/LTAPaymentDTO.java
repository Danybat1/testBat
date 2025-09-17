package com.freightops.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour les LTA éligibles au paiement
 * Optimise les performances en évitant la sérialisation complète des entités
 */
public class LTAPaymentDTO {

    private Long id;
    private String ltaNumber;
    private String trackingNumber;
    private String originCityName;
    private String destinationCityName;
    private String originCityIataCode;
    private String destinationCityIataCode;
    private String paymentMode;
    private String status;
    private BigDecimal calculatedCost;
    private String clientName;
    private String shipperName;
    private String consigneeName;
    private LocalDateTime createdAt;
    private Integer packageCount;
    private BigDecimal totalWeight;

    // Constructeurs
    public LTAPaymentDTO() {
    }

    public LTAPaymentDTO(Long id, String ltaNumber, String trackingNumber,
            String originCityName, String destinationCityName,
            String originCityIataCode, String destinationCityIataCode,
            String paymentMode, String status, BigDecimal calculatedCost,
            String clientName, String shipperName, String consigneeName,
            LocalDateTime createdAt, Integer packageCount, BigDecimal totalWeight) {
        this.id = id;
        this.ltaNumber = ltaNumber;
        this.trackingNumber = trackingNumber;
        this.originCityName = originCityName;
        this.destinationCityName = destinationCityName;
        this.originCityIataCode = originCityIataCode;
        this.destinationCityIataCode = destinationCityIataCode;
        this.paymentMode = paymentMode;
        this.status = status;
        this.calculatedCost = calculatedCost;
        this.clientName = clientName;
        this.shipperName = shipperName;
        this.consigneeName = consigneeName;
        this.createdAt = createdAt;
        this.packageCount = packageCount;
        this.totalWeight = totalWeight;
    }

    // Getters et Setters
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

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getOriginCityName() {
        return originCityName;
    }

    public void setOriginCityName(String originCityName) {
        this.originCityName = originCityName;
    }

    public String getDestinationCityName() {
        return destinationCityName;
    }

    public void setDestinationCityName(String destinationCityName) {
        this.destinationCityName = destinationCityName;
    }

    public String getOriginCityIataCode() {
        return originCityIataCode;
    }

    public void setOriginCityIataCode(String originCityIataCode) {
        this.originCityIataCode = originCityIataCode;
    }

    public String getDestinationCityIataCode() {
        return destinationCityIataCode;
    }

    public void setDestinationCityIataCode(String destinationCityIataCode) {
        this.destinationCityIataCode = destinationCityIataCode;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getCalculatedCost() {
        return calculatedCost;
    }

    public void setCalculatedCost(BigDecimal calculatedCost) {
        this.calculatedCost = calculatedCost;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getShipperName() {
        return shipperName;
    }

    public void setShipperName(String shipperName) {
        this.shipperName = shipperName;
    }

    public String getConsigneeName() {
        return consigneeName;
    }

    public void setConsigneeName(String consigneeName) {
        this.consigneeName = consigneeName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getPackageCount() {
        return packageCount;
    }

    public void setPackageCount(Integer packageCount) {
        this.packageCount = packageCount;
    }

    public BigDecimal getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(BigDecimal totalWeight) {
        this.totalWeight = totalWeight;
    }

    /**
     * Méthode utilitaire pour l'affichage du trajet
     */
    public String getRouteDisplay() {
        return originCityIataCode + " → " + destinationCityIataCode;
    }

    /**
     * Méthode utilitaire pour l'affichage complet
     */
    public String getDisplayText() {
        return ltaNumber + " (" + getRouteDisplay() + ") - " +
                (clientName != null ? clientName : shipperName);
    }
}
