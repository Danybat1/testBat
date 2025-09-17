package com.freightops.entity;

import com.freightops.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "treasury_transactions")
public class TreasuryTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La référence est obligatoire")
    @Column(nullable = false, unique = true)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être positif")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @NotBlank(message = "La description est obligatoire")
    @Column(nullable = false, length = 500)
    private String description;

    @Column(length = 100)
    private String category; // Catégorie de dépense/recette

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_box_id")
    private CashBox cashBox;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id")
    private BankAccount bankAccount;

    // Pour les transferts entre caisses/comptes
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_cash_box_id")
    private CashBox destinationCashBox;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_bank_account_id")
    private BankAccount destinationBankAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment; // Lien avec un paiement de facture

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public TreasuryTransaction() {}

    public TreasuryTransaction(String reference, TransactionType type, BigDecimal amount, 
                             LocalDate transactionDate, String description) {
        this.reference = reference;
        this.type = type;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public CashBox getCashBox() { return cashBox; }
    public void setCashBox(CashBox cashBox) { this.cashBox = cashBox; }

    public BankAccount getBankAccount() { return bankAccount; }
    public void setBankAccount(BankAccount bankAccount) { this.bankAccount = bankAccount; }

    public CashBox getDestinationCashBox() { return destinationCashBox; }
    public void setDestinationCashBox(CashBox destinationCashBox) { this.destinationCashBox = destinationCashBox; }

    public BankAccount getDestinationBankAccount() { return destinationBankAccount; }
    public void setDestinationBankAccount(BankAccount destinationBankAccount) { this.destinationBankAccount = destinationBankAccount; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
