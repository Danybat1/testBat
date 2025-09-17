package com.freightops.controller;

import com.freightops.entity.LTA;
import com.freightops.entity.LTAPayment;
import com.freightops.service.LTAPaymentService;
import com.freightops.repository.LTARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Optional;

// TEMPORAIRE: Contrôleur réactivé avec réponses vides pour éviter erreurs 404 frontend
@RestController
@RequestMapping("/api/lta-payments")
@CrossOrigin(origins = "*")
public class LTAPaymentController {

    // TEMPORAIRE: Injection désactivée
    // @Autowired
    private LTAPaymentService ltaPaymentService;

    @Autowired
    private LTARepository ltaRepository;

    /**
     * Récupère toutes les LTA non entièrement soldées
     */
    @GetMapping("/unpaid-ltas")
    public ResponseEntity<List<LTA>> getUnpaidLTAs() {
        try {
            // Retourne les LTA avec calculatedCost > 0 et statuts éligibles au paiement
            List<LTA> unpaidLTAs = ltaRepository.findLTAsEligibleForPayment();
            return ResponseEntity.ok(unpaidLTAs);
        } catch (Exception e) {
            // En cas d'erreur, retourne liste vide pour éviter crash frontend
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    /**
     * Récupère toutes les LTA non entièrement soldées (version optimisée avec DTO)
     */
    @GetMapping("/unpaid-ltas-dto")
    public ResponseEntity<List<com.freightops.dto.LTAPaymentDTO>> getUnpaidLTAsDTO() {
        try {
            // Utilise le DTO pour de meilleures performances et éviter les problèmes de
            // lazy loading
            List<com.freightops.dto.LTAPaymentDTO> unpaidLTAs = ltaRepository.findLTAsEligibleForPaymentAsDTO();
            return ResponseEntity.ok(unpaidLTAs);
        } catch (Exception e) {
            // En cas d'erreur, retourne liste vide pour éviter crash frontend
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    /**
     * Récupère le montant restant à payer pour une LTA
     */
    @GetMapping("/remaining-amount/{ltaId}")
    public ResponseEntity<Map<String, Object>> getRemainingAmount(@PathVariable Long ltaId) {
        try {
            Optional<LTA> ltaOpt = ltaRepository.findById(ltaId);
            if (ltaOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "LTA non trouvée"));
            }

            LTA lta = ltaOpt.get();
            BigDecimal totalCost = lta.getCalculatedCost() != null ? lta.getCalculatedCost() : BigDecimal.ZERO;
            // Pour l'instant, pas de paiements enregistrés, donc montant restant = coût
            // total
            BigDecimal remainingAmount = totalCost;

            return ResponseEntity.ok(Map.of(
                    "ltaId", ltaId,
                    "remainingAmount", remainingAmount,
                    "totalCost", totalCost,
                    "isFullyPaid", remainingAmount.compareTo(BigDecimal.ZERO) <= 0));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Erreur lors du calcul du montant restant"));
        }
    }

    /**
     * Enregistre un paiement pour une LTA
     */
    @PostMapping("/record-payment")
    public ResponseEntity<Map<String, Object>> recordPayment(@RequestBody PaymentRequest request) {
        try {
            // Validation de base
            if (request.getLtaId() == null || request.getAmount() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "LTA ID et montant requis"));
            }

            // Vérification que la LTA existe
            Optional<LTA> ltaOpt = ltaRepository.findById(request.getLtaId());
            if (ltaOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "LTA non trouvée"));
            }

            LTA lta = ltaOpt.get();
            BigDecimal totalCost = lta.getCalculatedCost() != null ? lta.getCalculatedCost() : BigDecimal.ZERO;

            // Validation du montant
            if (request.getAmount().compareTo(totalCost) > 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Le montant dépasse le coût total de la LTA"));
            }

            // Pour l'instant, on simule l'enregistrement du paiement
            // TODO: Implémenter la vraie logique de paiement quand le système sera réactivé

            // Génération d'une référence comptable temporaire
            String referenceComptable = "PAY-" + System.currentTimeMillis() + "-" + request.getLtaId();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Paiement enregistré avec succès (mode simulation)",
                    "ltaId", request.getLtaId(),
                    "amount", request.getAmount(),
                    "paymentMethod", request.getPaymentMethod(),
                    "referenceComptable", referenceComptable,
                    "remainingAmount", totalCost.subtract(request.getAmount())));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Erreur lors de l'enregistrement du paiement: " + e.getMessage()));
        }
    }

    /**
     * Récupère le résumé des paiements pour une LTA
     */
    @GetMapping("/summary/{ltaId}")
    public ResponseEntity<Map<String, Object>> getPaymentSummary(@PathVariable Long ltaId) {
        // TEMPORAIRE: Retourne résumé vide pour éviter erreur 404
        return ResponseEntity.ok(Map.of(
                "ltaId", ltaId,
                "totalCost", BigDecimal.ZERO,
                "totalPaid", BigDecimal.ZERO,
                "remainingAmount", BigDecimal.ZERO,
                "payments", Collections.emptyList(),
                "isFullyPaid", true));
    }

    /**
     * Récupère tous les paiements d'une LTA
     */
    @GetMapping("/by-lta/{ltaId}")
    public ResponseEntity<List<LTAPayment>> getPaymentsByLTA(@PathVariable Long ltaId) {
        // TEMPORAIRE: Retourne liste vide pour éviter erreur 404
        return ResponseEntity.ok(Collections.emptyList());
    }

    /**
     * Classe pour recevoir les données de paiement
     */
    public static class PaymentRequest {
        private Long ltaId;
        private BigDecimal amount;
        private String paymentMethod;
        private String reference;
        private String notes;
        private Long cashBoxId;

        // Getters and Setters
        public Long getLtaId() {
            return ltaId;
        }

        public void setLtaId(Long ltaId) {
            this.ltaId = ltaId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public Long getCashBoxId() {
            return cashBoxId;
        }

        public void setCashBoxId(Long cashBoxId) {
            this.cashBoxId = cashBoxId;
        }
    }
}
