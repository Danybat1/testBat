package com.freightops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

public class InvoiceItemDto {

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    private Integer quantity;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0", message = "Le prix unitaire doit être positif")
    private BigDecimal unitPrice;

    @NotNull(message = "Le prix total est obligatoire")
    private BigDecimal totalPrice;

    @NotNull(message = "Le taux de taxe est obligatoire")
    @DecimalMin(value = "0", message = "Le taux de taxe doit être positif")
    private BigDecimal taxRate;

    @NotNull(message = "Le montant de taxe est obligatoire")
    private BigDecimal taxAmount;

    private Long ltaId;
    private String ltaNumber;

    // Constructors
    public InvoiceItemDto() {
    }

    // Getters and Setters
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public Long getLtaId() {
        return ltaId;
    }

    public void setLtaId(Long ltaId) {
        this.ltaId = ltaId;
    }

    public String getLtaNumber() {
        return ltaNumber;
    }

    public void setLtaNumber(String ltaNumber) {
        this.ltaNumber = ltaNumber;
    }

    @Override
    public String toString() {
        return "InvoiceItemDto{" +
                "description='" + description + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                ", taxRate=" + taxRate +
                ", taxAmount=" + taxAmount +
                ", ltaId=" + ltaId +
                ", ltaNumber='" + ltaNumber + '\'' +
                '}';
    }
}
