package com.freightops.accounting.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité représentant un exercice comptable
 * Un exercice comptable est une période de 12 mois pour laquelle les comptes
 * sont établis
 */
@Entity
@Table(name = "fiscal_years")
public class FiscalYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year_number", unique = true, nullable = false)
    private Integer yearNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_closed", nullable = false)
    private Boolean isClosed = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructeurs
    public FiscalYear() {
    }

    public FiscalYear(Integer yearNumber, LocalDate startDate, LocalDate endDate) {
        this.yearNumber = yearNumber;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isClosed = false;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getYearNumber() {
        return yearNumber;
    }

    public void setYearNumber(Integer yearNumber) {
        this.yearNumber = yearNumber;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Boolean getIsClosed() {
        return isClosed;
    }

    public void setIsClosed(Boolean isClosed) {
        this.isClosed = isClosed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Vérifie si une date appartient à cet exercice
     * 
     * @param date la date à vérifier
     * @return true si la date est dans l'exercice
     */
    public boolean containsDate(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Vérifie si l'exercice est actuel (contient la date d'aujourd'hui)
     * 
     * @return true si l'exercice est actuel
     */
    public boolean isCurrent() {
        return containsDate(LocalDate.now());
    }
}
