package com.freightops.accounting.entity;

import com.freightops.accounting.enums.AccountType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un compte du plan comptable
 * Supporte une structure hiérarchique avec comptes parents et sous-comptes
 */
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false, length = 10)
    private String accountNumber;

    @Column(name = "account_name", nullable = false, length = 200)
    private String accountName;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_account_id")
    private Account parentAccount;

    @OneToMany(mappedBy = "parentAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Account> subAccounts = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "description")
    private String description;

    @Column(name = "balance", precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

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

    // Constructeurs
    public Account() {
    }

    public Account(String accountNumber, String accountName, AccountType accountType) {
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.accountType = accountType;
        this.isActive = true;
        this.balance = BigDecimal.ZERO;
    }

    public Account(String accountNumber, String accountName, AccountType accountType, Account parentAccount) {
        this(accountNumber, accountName, accountType);
        this.parentAccount = parentAccount;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public Account getParentAccount() {
        return parentAccount;
    }

    public void setParentAccount(Account parentAccount) {
        this.parentAccount = parentAccount;
    }

    public List<Account> getSubAccounts() {
        return subAccounts;
    }

    public void setSubAccounts(List<Account> subAccounts) {
        this.subAccounts = subAccounts;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
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

    // Méthodes métier
    /**
     * Vérifie si le compte est un compte de détail (sans sous-comptes)
     * 
     * @return true si le compte n'a pas de sous-comptes
     */
    public boolean isDetailAccount() {
        return subAccounts.isEmpty();
    }

    /**
     * Vérifie si le compte est un compte racine (sans parent)
     * 
     * @return true si le compte n'a pas de parent
     */
    public boolean isRootAccount() {
        return parentAccount == null;
    }

    /**
     * Retourne le chemin complet du compte (ex: "411.001")
     * 
     * @return le chemin hiérarchique du compte
     */
    public String getFullPath() {
        if (parentAccount == null) {
            return accountNumber;
        }
        return parentAccount.getFullPath() + "." + accountNumber;
    }

    /**
     * Retourne le nom complet du compte avec hiérarchie
     * 
     * @return le nom hiérarchique du compte
     */
    public String getFullName() {
        if (parentAccount == null) {
            return accountName;
        }
        return parentAccount.getFullName() + " > " + accountName;
    }

    /**
     * Met à jour le solde du compte
     * 
     * @param debitAmount  montant au débit
     * @param creditAmount montant au crédit
     */
    public void updateBalance(BigDecimal debitAmount, BigDecimal creditAmount) {
        if (accountType.increasesWithDebit()) {
            balance = balance.add(debitAmount).subtract(creditAmount);
        } else {
            balance = balance.add(creditAmount).subtract(debitAmount);
        }
    }
}
