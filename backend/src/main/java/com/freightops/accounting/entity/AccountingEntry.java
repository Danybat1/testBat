package com.freightops.accounting.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entité représentant une ligne d'écriture comptable
 * Chaque ligne contient un compte, un montant débit ou crédit, et une
 * description
 */
@Entity
@Table(name = "accounting_entries")
public class AccountingEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "debit_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal debitAmount = BigDecimal.ZERO;

    @Column(name = "credit_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal creditAmount = BigDecimal.ZERO;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "line_order", nullable = false)
    private Integer lineOrder;

    // Constructeurs
    public AccountingEntry() {
    }

    public AccountingEntry(JournalEntry journalEntry, Account account,
            BigDecimal debitAmount, BigDecimal creditAmount,
            String description, Integer lineOrder) {
        this.journalEntry = journalEntry;
        this.account = account;
        this.debitAmount = debitAmount != null ? debitAmount : BigDecimal.ZERO;
        this.creditAmount = creditAmount != null ? creditAmount : BigDecimal.ZERO;
        this.description = description;
        this.lineOrder = lineOrder;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JournalEntry getJournalEntry() {
        return journalEntry;
    }

    public void setJournalEntry(JournalEntry journalEntry) {
        this.journalEntry = journalEntry;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public BigDecimal getDebitAmount() {
        return debitAmount;
    }

    public void setDebitAmount(BigDecimal debitAmount) {
        this.debitAmount = debitAmount != null ? debitAmount : BigDecimal.ZERO;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount != null ? creditAmount : BigDecimal.ZERO;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getLineOrder() {
        return lineOrder;
    }

    public void setLineOrder(Integer lineOrder) {
        this.lineOrder = lineOrder;
    }

    // Méthodes métier
    /**
     * Vérifie si la ligne est au débit
     * 
     * @return true si le montant débit est supérieur à zéro
     */
    public boolean isDebit() {
        return debitAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Vérifie si la ligne est au crédit
     * 
     * @return true si le montant crédit est supérieur à zéro
     */
    public boolean isCredit() {
        return creditAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Retourne le montant net de la ligne (débit - crédit)
     * 
     * @return le montant net
     */
    public BigDecimal getNetAmount() {
        return debitAmount.subtract(creditAmount);
    }

    /**
     * Retourne le montant absolu de la ligne
     * 
     * @return le montant en valeur absolue
     */
    public BigDecimal getAbsoluteAmount() {
        return isDebit() ? debitAmount : creditAmount;
    }

    /**
     * Valide que la ligne n'a qu'un seul côté (débit OU crédit, pas les deux)
     * 
     * @return true si la ligne est valide
     */
    public boolean isValid() {
        boolean hasDebit = debitAmount.compareTo(BigDecimal.ZERO) > 0;
        boolean hasCredit = creditAmount.compareTo(BigDecimal.ZERO) > 0;

        // Une ligne doit avoir soit un débit, soit un crédit, mais pas les deux
        return hasDebit ^ hasCredit;
    }

    /**
     * Met à jour le solde du compte associé
     */
    public void updateAccountBalance() {
        if (account != null) {
            account.updateBalance(debitAmount, creditAmount);
        }
    }
}
