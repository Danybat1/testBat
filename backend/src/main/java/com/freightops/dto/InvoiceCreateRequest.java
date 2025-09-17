package com.freightops.dto;

import com.freightops.enums.InvoiceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.List;

public class InvoiceCreateRequest {

    @NotNull(message = "L'ID du client est obligatoire")
    private Long clientId;

    @NotNull(message = "La date de facture est obligatoire")
    private LocalDate invoiceDate;

    @NotNull(message = "La date d'échéance est obligatoire")
    private LocalDate dueDate;

    @NotNull(message = "Le type de facture est obligatoire")
    private InvoiceType type;

    @NotBlank(message = "Les conditions de paiement sont obligatoires")
    private String paymentTerms;

    @Valid
    @NotNull(message = "L'adresse de facturation est obligatoire")
    private InvoiceAddressDto billingAddress;

    @Valid
    @NotEmpty(message = "Au moins un article est obligatoire")
    private List<InvoiceItemDto> items;

    private String notes;

    @NotBlank(message = "La devise est obligatoire")
    private String currency;

    private List<Long> ltaIds;

    // Constructors
    public InvoiceCreateRequest() {
    }

    // Getters and Setters
    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public InvoiceType getType() {
        return type;
    }

    public void setType(InvoiceType type) {
        this.type = type;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public InvoiceAddressDto getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(InvoiceAddressDto billingAddress) {
        this.billingAddress = billingAddress;
    }

    public List<InvoiceItemDto> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItemDto> items) {
        this.items = items;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<Long> getLtaIds() {
        return ltaIds;
    }

    public void setLtaIds(List<Long> ltaIds) {
        this.ltaIds = ltaIds;
    }

    @Override
    public String toString() {
        return "InvoiceCreateRequest{" +
                "clientId=" + clientId +
                ", invoiceDate=" + invoiceDate +
                ", dueDate=" + dueDate +
                ", type=" + type +
                ", paymentTerms='" + paymentTerms + '\'' +
                ", billingAddress=" + billingAddress +
                ", items=" + items +
                ", notes='" + notes + '\'' +
                ", currency='" + currency + '\'' +
                ", ltaIds=" + ltaIds +
                '}';
    }
}
