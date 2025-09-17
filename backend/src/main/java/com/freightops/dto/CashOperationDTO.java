package com.freightops.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CashOperationDTO {
    private Long id;
    private String reference;
    private LocalDate operationDate;
    private String operationType;
    private String clientSupplier;
    private String description;
    private BigDecimal amount;
    private String currency;
    private BigDecimal balance; // Solde après cette opération
    private String paymentMethod;
    private String category;

    // Constructors
    public CashOperationDTO() {
    }

    public CashOperationDTO(Long id, String reference, LocalDate operationDate,
            String operationType, String clientSupplier, String description,
            BigDecimal amount, String currency, BigDecimal balance) {
        this.id = id;
        this.reference = reference;
        this.operationDate = operationDate;
        this.operationType = operationType;
        this.clientSupplier = clientSupplier;
        this.description = description;
        this.amount = amount;
        this.currency = currency;
        this.balance = balance;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public LocalDate getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(LocalDate operationDate) {
        this.operationDate = operationDate;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getClientSupplier() {
        return clientSupplier;
    }

    public void setClientSupplier(String clientSupplier) {
        this.clientSupplier = clientSupplier;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
