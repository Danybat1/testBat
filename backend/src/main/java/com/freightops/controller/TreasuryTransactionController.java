package com.freightops.controller;

import com.freightops.entity.TreasuryTransaction;
import com.freightops.service.TreasuryTransactionService;
import com.freightops.service.TreasuryReportService;
import com.freightops.enums.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/treasury-transactions")
@CrossOrigin(origins = "*")
public class TreasuryTransactionController {

    @Autowired
    private TreasuryTransactionService treasuryTransactionService;

    @Autowired
    private TreasuryReportService treasuryReportService;

    @GetMapping
    public ResponseEntity<List<TreasuryTransaction>> getAllTransactions() {
        List<TreasuryTransaction> transactions = treasuryTransactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<TreasuryTransaction>> getTransactionsByType(@PathVariable TransactionType type) {
        List<TreasuryTransaction> transactions = treasuryTransactionService.getTransactionsByType(type);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<TreasuryTransaction>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TreasuryTransaction> transactions = treasuryTransactionService.getTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/cash-box/{cashBoxId}")
    public ResponseEntity<List<TreasuryTransaction>> getTransactionsByCashBox(@PathVariable Long cashBoxId) {
        List<TreasuryTransaction> transactions = treasuryTransactionService.getTransactionsByCashBox(cashBoxId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/bank-account/{bankAccountId}")
    public ResponseEntity<List<TreasuryTransaction>> getTransactionsByBankAccount(@PathVariable Long bankAccountId) {
        List<TreasuryTransaction> transactions = treasuryTransactionService.getTransactionsByBankAccount(bankAccountId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<TreasuryTransaction>> getTransactionsByCategory(@PathVariable String category) {
        List<TreasuryTransaction> transactions = treasuryTransactionService.getTransactionsByCategory(category);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = treasuryTransactionService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TreasuryTransaction> getTransactionById(@PathVariable Long id) {
        return treasuryTransactionService.getTransactionById(id)
                .map(transaction -> ResponseEntity.ok(transaction))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<TreasuryTransaction> getTransactionByReference(@PathVariable String reference) {
        return treasuryTransactionService.getTransactionByReference(reference)
                .map(transaction -> ResponseEntity.ok(transaction))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats/income")
    public ResponseEntity<BigDecimal> getTotalIncomeByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BigDecimal total = treasuryTransactionService.getTotalIncomeByDateRange(startDate, endDate);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/expense")
    public ResponseEntity<BigDecimal> getTotalExpenseByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BigDecimal total = treasuryTransactionService.getTotalExpenseByDateRange(startDate, endDate);
        return ResponseEntity.ok(total);
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(@Valid @RequestBody TreasuryTransaction transaction) {
        try {
            TreasuryTransaction createdTransaction = treasuryTransactionService.createTransaction(transaction);
            return ResponseEntity.ok(createdTransaction);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransaction(@PathVariable Long id, @Valid @RequestBody TreasuryTransaction transactionDetails) {
        try {
            TreasuryTransaction updatedTransaction = treasuryTransactionService.updateTransaction(id, transactionDetails);
            return ResponseEntity.ok(updatedTransaction);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id) {
        try {
            treasuryTransactionService.deleteTransaction(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> createTransfer(@RequestBody Map<String, Object> transferRequest) {
        try {
            Long sourceCashBoxId = transferRequest.get("sourceCashBoxId") != null ? 
                    Long.valueOf(transferRequest.get("sourceCashBoxId").toString()) : null;
            Long sourceAccountId = transferRequest.get("sourceAccountId") != null ? 
                    Long.valueOf(transferRequest.get("sourceAccountId").toString()) : null;
            Long destCashBoxId = transferRequest.get("destCashBoxId") != null ? 
                    Long.valueOf(transferRequest.get("destCashBoxId").toString()) : null;
            Long destAccountId = transferRequest.get("destAccountId") != null ? 
                    Long.valueOf(transferRequest.get("destAccountId").toString()) : null;
            BigDecimal amount = new BigDecimal(transferRequest.get("amount").toString());
            String description = (String) transferRequest.get("description");

            TreasuryTransaction transfer = treasuryTransactionService.createTransfer(
                    sourceCashBoxId, sourceAccountId, destCashBoxId, destAccountId, amount, description);
            return ResponseEntity.ok(transfer);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/reports/treasury")
    public ResponseEntity<byte[]> generateTreasuryReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            byte[] pdfBytes = treasuryReportService.generateTreasuryReport(startDate, endDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "rapport-tresorerie-" + startDate + "-" + endDate + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/reports/cash-flow")
    public ResponseEntity<byte[]> generateCashFlowReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            byte[] pdfBytes = treasuryReportService.generateCashFlowReport(startDate, endDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "rapport-flux-tresorerie-" + startDate + "-" + endDate + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
