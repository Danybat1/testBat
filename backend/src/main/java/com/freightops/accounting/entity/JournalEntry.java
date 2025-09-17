package com.freightops.accounting.entity;

import com.freightops.accounting.enums.SourceType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant l'en-tête d'une écriture comptable
 * Contient les informations générales et les lignes de détail
 */
@Entity
@Table(name = "journal_entries")
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entry_number", unique = true, nullable = false, length = 50)
    private String entryNumber;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "reference", length = 100)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscal_year_id", nullable = false)
    private FiscalYear fiscalYear;

    @Column(name = "total_debit", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalDebit = BigDecimal.ZERO;

    @Column(name = "total_credit", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalCredit = BigDecimal.ZERO;

    @Column(name = "is_balanced", nullable = false)
    private Boolean isBalanced = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type")
    private SourceType sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("lineOrder ASC")
    private List<AccountingEntry> accountingEntries = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (entryNumber == null) {
            generateEntryNumber();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        calculateTotals();
        validateBalance();
    }

    // Constructeurs
    public JournalEntry() {
    }

    public JournalEntry(LocalDate entryDate, String description, FiscalYear fiscalYear) {
        this.entryDate = entryDate;
        this.description = description;
        this.fiscalYear = fiscalYear;
    }

    public JournalEntry(LocalDate entryDate, String description, FiscalYear fiscalYear,
            SourceType sourceType, Long sourceId) {
        this(entryDate, description, fiscalYear);
        this.sourceType = sourceType;
        this.sourceId = sourceId;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntryNumber() {
        return entryNumber;
    }

    public void setEntryNumber(String entryNumber) {
        this.entryNumber = entryNumber;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public FiscalYear getFiscalYear() {
        return fiscalYear;
    }

    public void setFiscalYear(FiscalYear fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    public BigDecimal getTotalDebit() {
        return totalDebit;
    }

    public void setTotalDebit(BigDecimal totalDebit) {
        this.totalDebit = totalDebit;
    }

    public BigDecimal getTotalCredit() {
        return totalCredit;
    }

    public void setTotalCredit(BigDecimal totalCredit) {
        this.totalCredit = totalCredit;
    }

    public Boolean getIsBalanced() {
        return isBalanced;
    }

    public void setIsBalanced(Boolean isBalanced) {
        this.isBalanced = isBalanced;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<AccountingEntry> getAccountingEntries() {
        return accountingEntries;
    }

    public void setAccountingEntries(List<AccountingEntry> accountingEntries) {
        this.accountingEntries = accountingEntries;
    }

    // Méthodes métier
    /**
     * Ajoute une ligne d'écriture comptable
     * 
     * @param account      le compte à imputer
     * @param debitAmount  montant au débit
     * @param creditAmount montant au crédit
     * @param description  description de la ligne
     */
    public void addAccountingEntry(Account account, BigDecimal debitAmount,
            BigDecimal creditAmount, String description) {
        AccountingEntry entry = new AccountingEntry();
        entry.setJournalEntry(this);
        entry.setAccount(account);
        entry.setDebitAmount(debitAmount != null ? debitAmount : BigDecimal.ZERO);
        entry.setCreditAmount(creditAmount != null ? creditAmount : BigDecimal.ZERO);
        entry.setDescription(description);
        entry.setLineOrder(accountingEntries.size() + 1);

        accountingEntries.add(entry);
        calculateTotals();
    }

    /**
     * Calcule les totaux débit et crédit
     */
    public void calculateTotals() {
        totalDebit = accountingEntries.stream()
                .map(AccountingEntry::getDebitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalCredit = accountingEntries.stream()
                .map(AccountingEntry::getCreditAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Valide l'équilibre de l'écriture (débit = crédit)
     */
    public void validateBalance() {
        isBalanced = totalDebit.compareTo(totalCredit) == 0;
    }

    /**
     * Génère automatiquement le numéro d'écriture
     */
    private void generateEntryNumber() {
        if (fiscalYear != null && entryDate != null) {
            // Format: JE-YYYY-NNNNNN (ex: JE-2024-000001)
            String year = String.valueOf(fiscalYear.getYearNumber());
            // Le numéro séquentiel sera généré par le service
            this.entryNumber = "JE-" + year + "-TEMP";
        }
    }

    /**
     * Vérifie si l'écriture est automatique
     * 
     * @return true si l'écriture provient d'un processus automatique
     */
    public boolean isAutomatic() {
        return sourceType != null && sourceType.isAutomatic();
    }
}
