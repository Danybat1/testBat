package com.freightops.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CashStatementDTO {
    private PeriodInfo period;
    private BigDecimal openingBalance;
    private BigDecimal totalEncaissements;
    private BigDecimal totalDecaissements;
    private BigDecimal closingBalance;
    private String currency;
    private List<CashOperationDTO> operations;
    private String cashBoxName;
    private LocalDate startDate;
    private LocalDate endDate;

    // Constructors
    public CashStatementDTO() {
    }

    public CashStatementDTO(PeriodInfo period, BigDecimal openingBalance,
            BigDecimal totalEncaissements, BigDecimal totalDecaissements,
            BigDecimal closingBalance, String currency,
            List<CashOperationDTO> operations, String cashBoxName, LocalDate startDate, LocalDate endDate) {
        this.period = period;
        this.openingBalance = openingBalance;
        this.totalEncaissements = totalEncaissements;
        this.totalDecaissements = totalDecaissements;
        this.closingBalance = closingBalance;
        this.currency = currency;
        this.operations = operations;
        this.cashBoxName = cashBoxName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public PeriodInfo getPeriod() {
        return period;
    }

    public void setPeriod(PeriodInfo period) {
        this.period = period;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }

    public BigDecimal getTotalEncaissements() {
        return totalEncaissements;
    }

    public void setTotalEncaissements(BigDecimal totalEncaissements) {
        this.totalEncaissements = totalEncaissements;
    }

    public BigDecimal getTotalDecaissements() {
        return totalDecaissements;
    }

    public void setTotalDecaissements(BigDecimal totalDecaissements) {
        this.totalDecaissements = totalDecaissements;
    }

    public BigDecimal getClosingBalance() {
        return closingBalance;
    }

    public void setClosingBalance(BigDecimal closingBalance) {
        this.closingBalance = closingBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<CashOperationDTO> getOperations() {
        return operations;
    }

    public void setOperations(List<CashOperationDTO> operations) {
        this.operations = operations;
    }

    public String getCashBoxName() {
        return cashBoxName;
    }

    public void setCashBoxName(String cashBoxName) {
        this.cashBoxName = cashBoxName;
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

    // Inner class for period information
    public static class PeriodInfo {
        private LocalDate startDate;
        private LocalDate endDate;

        public PeriodInfo() {
        }

        public PeriodInfo(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
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
    }
}
