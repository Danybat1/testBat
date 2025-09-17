package com.freightops.entity;

import com.freightops.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lta_payments")
public class LTAPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lta_id", nullable = false)
    @NotNull(message = "La LTA est obligatoire")
    private LTA lta;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être positif")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(length = 50, nullable = false)
    private String paymentMethod; // ESPECES, PORT_DU

    @Column(length = 100)
    private String reference;

    @Column(length = 500)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.COMPLETED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_box_id")
    private CashBox cashBox;

    // Comptabilité double écriture
    @Column(name = "debit_account", length = 20)
    private String debitAccount; // Compte débité (ex: 5111 - Caisse)

    @Column(name = "credit_account", length = 20)
    private String creditAccount; // Compte crédité (ex: 7061 - Ventes transport)

    @Column(name = "accounting_reference", length = 50)
    private String accountingReference; // Référence comptable

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Génération automatique des comptes comptables
        if (debitAccount == null) {
            if ("ESPECES".equals(paymentMethod)) {
                debitAccount = "5111"; // Caisse espèces
            } else if ("PORT_DU".equals(paymentMethod)) {
                debitAccount = "4111"; // Clients - Port dû
            }
        }

        if (creditAccount == null) {
            creditAccount = "7061"; // Ventes de services transport
        }

        if (accountingReference == null) {
            accountingReference = "LTA-PAY-" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public LTAPayment() {
    }

    public LTAPayment(LTA lta, BigDecimal amount, LocalDate paymentDate, String paymentMethod) {
        this.lta = lta;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LTA getLta() {
        return lta;
    }

    public void setLta(LTA lta) {
        this.lta = lta;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public CashBox getCashBox() {
        return cashBox;
    }

    public void setCashBox(CashBox cashBox) {
        this.cashBox = cashBox;
    }

    public String getDebitAccount() {
        return debitAccount;
    }

    public void setDebitAccount(String debitAccount) {
        this.debitAccount = debitAccount;
    }

    public String getCreditAccount() {
        return creditAccount;
    }

    public void setCreditAccount(String creditAccount) {
        this.creditAccount = creditAccount;
    }

    public String getAccountingReference() {
        return accountingReference;
    }

    public void setAccountingReference(String accountingReference) {
        this.accountingReference = accountingReference;
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
}
