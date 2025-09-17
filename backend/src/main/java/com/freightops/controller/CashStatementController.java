package com.freightops.controller;

import com.freightops.dto.CashStatementDTO;
import com.freightops.entity.CashBox;
import com.freightops.entity.TreasuryTransaction;
import com.freightops.enums.TransactionType;
import com.freightops.repository.CashBoxRepository;
import com.freightops.repository.TreasuryTransactionRepository;
import com.freightops.service.CashStatementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cash-operations")
@CrossOrigin(origins = "*")
public class CashStatementController {

    @Autowired
    private CashStatementService cashStatementService;

    @Autowired
    private CashBoxRepository cashBoxRepository;

    @Autowired
    private TreasuryTransactionRepository treasuryTransactionRepository;

    @GetMapping("/statement")
    public ResponseEntity<CashStatementDTO> getCashStatement(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "CDF") String currency,
            @RequestParam(required = false) Long cashBoxId) {

        try {
            System.out.println("Paramètres reçus - startDate: " + startDate + ", endDate: " + endDate + ", currency: "
                    + currency + ", cashBoxId: " + cashBoxId);
            CashStatementDTO statement = cashStatementService.getCashStatement(startDate, endDate, currency, cashBoxId);
            return ResponseEntity.ok(statement);
        } catch (Exception e) {
            System.err.println("Erreur dans getCashStatement: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/cash-boxes")
    public ResponseEntity<List<CashBox>> getActiveCashBoxes() {
        try {
            List<CashBox> activeCashBoxes = cashBoxRepository.findByActiveTrue();
            return ResponseEntity.ok(activeCashBoxes);
        } catch (Exception e) {
            System.err.println("Erreur dans getActiveCashBoxes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/initialize-cash-box")
    public ResponseEntity<String> initializeCashBox() {
        try {
            // Vérifier s'il existe déjà une caisse active
            List<CashBox> existingCashBoxes = cashBoxRepository.findByActiveTrue();
            if (!existingCashBoxes.isEmpty()) {
                return ResponseEntity.ok("Caisse active déjà existante: " + existingCashBoxes.get(0).getName());
            }

            // Créer une caisse par défaut pour les tests
            CashBox defaultCashBox = new CashBox();
            defaultCashBox.setName("Caisse Principale");
            defaultCashBox.setDescription("Caisse principale pour les opérations de trésorerie");
            defaultCashBox.setInitialBalance(new BigDecimal("1000.00"));
            defaultCashBox.setCurrentBalance(new BigDecimal("1000.00"));
            defaultCashBox.setActive(true);

            CashBox savedCashBox = cashBoxRepository.save(defaultCashBox);
            return ResponseEntity
                    .ok("Caisse créée avec succès: " + savedCashBox.getName() + " (ID: " + savedCashBox.getId() + ")");
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la caisse: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    @PostMapping("/record-operation")
    public ResponseEntity<Map<String, Object>> recordCashOperation(@RequestBody Map<String, Object> operationData) {
        try {
            System.out.println("Données d'opération reçues: " + operationData);

            // Récupérer ou créer une caisse active
            CashBox cashBox = cashBoxRepository.findFirstByActiveTrue()
                    .orElseThrow(() -> new RuntimeException("Aucune caisse active trouvée"));

            // Créer une transaction de trésorerie
            TreasuryTransaction transaction = new TreasuryTransaction();
            transaction.setReference((String) operationData.get("reference"));
            transaction.setTransactionDate(LocalDate.parse((String) operationData.get("operationDate")));
            transaction.setDescription((String) operationData.get("description"));
            transaction.setAmount(new BigDecimal(operationData.get("amount").toString()));
            transaction.setCashBox(cashBox);

            // Déterminer le type de transaction
            String operationType = (String) operationData.get("operationType");
            if ("ENCAISSEMENT".equals(operationType)) {
                transaction.setType(TransactionType.INCOME);
            } else if ("DECAISSEMENT".equals(operationType)) {
                transaction.setType(TransactionType.EXPENSE);
            } else {
                transaction.setType(TransactionType.INCOME); // Par défaut
            }

            // Sauvegarder la transaction
            TreasuryTransaction savedTransaction = treasuryTransactionRepository.save(transaction);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transactionId", savedTransaction.getId());
            response.put("message", "Opération enregistrée avec succès");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement de l'opération: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/test-create-operation")
    public ResponseEntity<Map<String, Object>> testCreateOperation() {
        try {
            // Récupérer la caisse active
            CashBox cashBox = cashBoxRepository.findFirstByActiveTrue()
                    .orElseThrow(() -> new RuntimeException("Aucune caisse active trouvée"));

            // Créer une transaction de test
            TreasuryTransaction testTransaction = new TreasuryTransaction();
            testTransaction.setReference("TEST-" + System.currentTimeMillis());
            testTransaction.setTransactionDate(LocalDate.now());
            testTransaction.setDescription("Test d'opération de caisse");
            testTransaction.setAmount(new BigDecimal("100.00"));
            testTransaction.setCashBox(cashBox);
            testTransaction.setType(TransactionType.INCOME);

            // Sauvegarder
            TreasuryTransaction saved = treasuryTransactionRepository.save(testTransaction);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Transaction de test créée");
            response.put("transactionId", saved.getId());
            response.put("reference", saved.getReference());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la transaction de test: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}
