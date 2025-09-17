package com.freightops.service;

import com.freightops.entity.TreasuryTransaction;
import com.freightops.repository.TreasuryTransactionRepository;
import com.freightops.repository.CashBoxRepository;
import com.freightops.repository.BankAccountRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TreasuryReportService {

    private static final String TREASURY_REPORT_TEMPLATE_PATH = "reports/treasury_report_template.jrxml";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private TreasuryTransactionRepository transactionRepository;

    @Autowired
    private CashBoxRepository cashBoxRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    public byte[] generateTreasuryReport(LocalDate startDate, LocalDate endDate) throws Exception {
        // Load the template
        ClassPathResource resource = new ClassPathResource(TREASURY_REPORT_TEMPLATE_PATH);
        InputStream templateStream = resource.getInputStream();

        // Compile the report
        JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);

        // Get transactions for the period
        List<TreasuryTransaction> transactions = transactionRepository.findByTransactionDateBetween(startDate, endDate);

        // Calculate totals using existing repository methods
        BigDecimal totalCashBalance = cashBoxRepository.findByActiveTrue().stream()
            .map(cb -> cb.getCurrentBalance())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalBankBalance = bankAccountRepository.findByActiveTrue().stream()
            .map(ba -> ba.getCurrentBalance())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalBalance = totalCashBalance.add(totalBankBalance);

        BigDecimal totalIncome = transactions.stream()
            .filter(t -> t.getType() == com.freightops.enums.TransactionType.INCOME)
            .map(TreasuryTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpense = transactions.stream()
            .filter(t -> t.getType() == com.freightops.enums.TransactionType.EXPENSE)
            .map(TreasuryTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netFlow = totalIncome.subtract(totalExpense);

        // Prepare parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("reportTitle", "RAPPORT DE TRÉSORERIE");
        parameters.put("reportPeriod", startDate.format(DATE_FORMATTER) + " - " + endDate.format(DATE_FORMATTER));
        parameters.put("totalCashBalance", totalCashBalance);
        parameters.put("totalBankBalance", totalBankBalance);
        parameters.put("totalBalance", totalBalance);
        parameters.put("totalIncome", totalIncome);
        parameters.put("totalExpense", totalExpense);
        parameters.put("netFlow", netFlow);

        // Prepare data source for transactions
        List<TreasuryTransactionData> transactionDataList = transactions.stream()
                .map(this::convertToTransactionData)
                .toList();
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(transactionDataList);

        // Fill the report
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        // Export to PDF
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    public byte[] generateCashFlowReport(LocalDate startDate, LocalDate endDate) throws Exception {
        // Similar to treasury report but focused on cash flow
        return generateTreasuryReport(startDate, endDate);
    }

    private TreasuryTransactionData convertToTransactionData(TreasuryTransaction transaction) {
        TreasuryTransactionData data = new TreasuryTransactionData();
        data.setReference(transaction.getReference());
        data.setType(getTypeLabel(transaction.getType()));
        data.setAmount(transaction.getAmount());
        data.setTransactionDate(transaction.getTransactionDate().format(DATE_FORMATTER));
        data.setDescription(transaction.getDescription());
        data.setCategory(transaction.getCategory());

        // Determine account name and type
        if (transaction.getCashBox() != null) {
            data.setAccountName(transaction.getCashBox().getName());
            data.setAccountType("Caisse");
        } else if (transaction.getBankAccount() != null) {
            data.setAccountName(transaction.getBankAccount().getAccountName());
            data.setAccountType("Banque");
        } else {
            data.setAccountName("-");
            data.setAccountType("-");
        }

        return data;
    }

    private String getTypeLabel(com.freightops.enums.TransactionType type) {
        switch (type) {
            case INCOME: return "Recette";
            case EXPENSE: return "Dépense";
            case TRANSFER: return "Transfert";
            default: return type.toString();
        }
    }

    // Data class for JasperReports
    public static class TreasuryTransactionData {
        private String reference;
        private String type;
        private BigDecimal amount;
        private String transactionDate;
        private String description;
        private String category;
        private String accountName;
        private String accountType;

        // Getters and setters
        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getTransactionDate() { return transactionDate; }
        public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getAccountName() { return accountName; }
        public void setAccountName(String accountName) { this.accountName = accountName; }

        public String getAccountType() { return accountType; }
        public void setAccountType(String accountType) { this.accountType = accountType; }
    }
}
