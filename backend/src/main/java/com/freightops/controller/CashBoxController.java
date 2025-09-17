package com.freightops.controller;

import com.freightops.entity.CashBox;
import com.freightops.entity.TreasuryTransaction;
import com.freightops.service.CashBoxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cash-boxes")
@CrossOrigin(origins = "*")
public class CashBoxController {

    @Autowired
    private CashBoxService cashBoxService;

    @GetMapping
    public ResponseEntity<List<CashBox>> getAllCashBoxes() {
        List<CashBox> cashBoxes = cashBoxService.getAllCashBoxes();
        return ResponseEntity.ok(cashBoxes);
    }

    @GetMapping("/active")
    public ResponseEntity<List<CashBox>> getActiveCashBoxes() {
        List<CashBox> cashBoxes = cashBoxService.getActiveCashBoxes();
        return ResponseEntity.ok(cashBoxes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CashBox> getCashBoxById(@PathVariable Long id) {
        return cashBoxService.getCashBoxById(id)
                .map(cashBox -> ResponseEntity.ok(cashBox))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<CashBox> getCashBoxByName(@PathVariable String name) {
        CashBox cashBox = cashBoxService.getCashBoxByName(name);
        return cashBox != null ? ResponseEntity.ok(cashBox) : ResponseEntity.notFound().build();
    }

    @GetMapping("/stats/total-balance")
    public ResponseEntity<BigDecimal> getTotalCashBalance() {
        BigDecimal total = cashBoxService.getTotalCashBalance();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/low-balance")
    public ResponseEntity<List<CashBox>> getCashBoxesWithLowBalance(@RequestParam BigDecimal threshold) {
        List<CashBox> cashBoxes = cashBoxService.getCashBoxesWithLowBalance(threshold);
        return ResponseEntity.ok(cashBoxes);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getCashBoxBalance(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            BigDecimal balance = cashBoxService.getCashBoxBalance(id, date);
            return ResponseEntity.ok(balance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TreasuryTransaction>> getCashBoxTransactions(@PathVariable Long id) {
        try {
            List<TreasuryTransaction> transactions = cashBoxService.getCashBoxTransactions(id);
            return ResponseEntity.ok(transactions);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/transactions/date-range")
    public ResponseEntity<List<TreasuryTransaction>> getCashBoxTransactionsByDateRange(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<TreasuryTransaction> transactions = cashBoxService.getCashBoxTransactionsByDateRange(id, startDate, endDate);
            return ResponseEntity.ok(transactions);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createCashBox(@Valid @RequestBody CashBox cashBox) {
        try {
            CashBox createdCashBox = cashBoxService.createCashBox(cashBox);
            return ResponseEntity.ok(createdCashBox);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCashBox(@PathVariable Long id, @Valid @RequestBody CashBox cashBoxDetails) {
        try {
            CashBox updatedCashBox = cashBoxService.updateCashBox(id, cashBoxDetails);
            return ResponseEntity.ok(updatedCashBox);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCashBox(@PathVariable Long id) {
        try {
            cashBoxService.deleteCashBox(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateCashBox(@PathVariable Long id) {
        try {
            CashBox activatedCashBox = cashBoxService.activateCashBox(id);
            return ResponseEntity.ok(activatedCashBox);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateCashBox(@PathVariable Long id) {
        try {
            CashBox deactivatedCashBox = cashBoxService.deactivateCashBox(id);
            return ResponseEntity.ok(deactivatedCashBox);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/adjust-balance")
    public ResponseEntity<?> adjustBalance(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String reason = (String) request.get("reason");
            CashBox adjustedCashBox = cashBoxService.adjustBalance(id, amount, reason);
            return ResponseEntity.ok(adjustedCashBox);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
