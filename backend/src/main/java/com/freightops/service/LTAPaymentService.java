package com.freightops.service;

import com.freightops.dto.LTAPaymentDTO;
import com.freightops.entity.LTA;
import com.freightops.entity.LTAPayment;
import com.freightops.repository.LTARepository;
import com.freightops.repository.LTAPaymentRepository;
import com.freightops.accounting.service.AccountingService;
import com.freightops.accounting.entity.Account;
import com.freightops.accounting.entity.JournalEntry;
import com.freightops.accounting.entity.FiscalYear;
import com.freightops.accounting.service.AccountService;
import com.freightops.accounting.service.FiscalYearService;
import com.freightops.accounting.service.JournalEntryService;
import com.freightops.accounting.enums.SourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Service dédié à la gestion des paiements LTA
 * Centralise la logique métier des paiements
 */
@Service
@Transactional
public class LTAPaymentService {

    @Autowired
    private LTARepository ltaRepository;

    @Autowired
    private LTAPaymentRepository ltaPaymentRepository;

    @Autowired
    private AccountingService accountingService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private FiscalYearService fiscalYearService;

    @Autowired
    private JournalEntryService journalEntryService;

    private static final Logger LOGGER = Logger.getLogger(LTAPaymentService.class.getName());

    /**
     * Récupère toutes les LTA éligibles au paiement
     * 
     * @return Liste des LTA éligibles
     */
    public List<LTAPaymentDTO> getUnpaidLTAs() {
        return ltaRepository.findLTAsEligibleForPaymentAsDTO();
    }

    /**
     * Calcule le montant restant à payer pour une LTA
     * 
     * @param ltaId ID de la LTA
     * @return Informations sur le montant restant
     */
    public Map<String, Object> calculateRemainingAmount(Long ltaId) {
        Optional<LTA> ltaOpt = ltaRepository.findById(ltaId);

        if (ltaOpt.isEmpty()) {
            throw new IllegalArgumentException("LTA non trouvée avec l'ID: " + ltaId);
        }

        LTA lta = ltaOpt.get();
        BigDecimal totalCost = lta.getCalculatedCost() != null ? lta.getCalculatedCost() : BigDecimal.ZERO;

        // Pour l'instant, pas de paiements enregistrés, donc montant restant = coût
        // total
        BigDecimal remainingAmount = totalCost;

        return Map.of(
                "ltaId", ltaId,
                "remainingAmount", remainingAmount,
                "totalCost", totalCost,
                "isFullyPaid", remainingAmount.compareTo(BigDecimal.ZERO) <= 0);
    }

    /**
     * Enregistre un paiement pour une LTA
     * 
     * @param ltaId         ID de la LTA
     * @param amount        Montant du paiement
     * @param paymentMethod Méthode de paiement
     * @return Résultat du paiement
     */
    public Map<String, Object> recordPayment(Long ltaId, BigDecimal amount, String paymentMethod) {
        // Validation des paramètres
        if (ltaId == null || amount == null) {
            throw new IllegalArgumentException("LTA ID et montant requis");
        }

        // Vérification que la LTA existe
        Optional<LTA> ltaOpt = ltaRepository.findById(ltaId);
        if (ltaOpt.isEmpty()) {
            throw new IllegalArgumentException("LTA non trouvée");
        }

        LTA lta = ltaOpt.get();
        BigDecimal totalCost = lta.getCalculatedCost() != null ? lta.getCalculatedCost() : BigDecimal.ZERO;

        // Validation du montant
        if (amount.compareTo(totalCost) > 0) {
            throw new IllegalArgumentException("Le montant dépasse le coût total de la LTA");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }

        // Génération d'une référence comptable
        String referenceComptable = generateAccountingReference(ltaId);

        // Enregistrement du paiement
        LTAPayment payment = new LTAPayment();
        payment.setLta(lta);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setReference(referenceComptable);
        payment.setPaymentDate(LocalDate.now());
        ltaPaymentRepository.save(payment);

        // Intégration comptable automatique
        integrateAccounting(payment);

        return Map.of(
                "success", true,
                "message", "Paiement enregistré avec succès",
                "ltaId", ltaId,
                "amount", amount,
                "paymentMethod", paymentMethod,
                "referenceComptable", referenceComptable,
                "remainingAmount", totalCost.subtract(amount),
                "paymentDate", LocalDate.now());
    }

    /**
     * Intègre le paiement dans la comptabilité
     * 
     * @param payment Paiement à intégrer
     */
    private void integrateAccounting(LTAPayment payment) {
        try {
            // Récupération de l'année fiscale en cours
            FiscalYear fiscalYear = fiscalYearService.getCurrentFiscalYear();

            // Récupération des comptes comptables par numéro
            Account treasuryAccount = accountService.getAccountByNumber("531"); // 531 Caisse
            Account clientAccount = accountService.getAccountByNumber("411"); // 411 Clients

            // Vérification que les comptes existent
            if (treasuryAccount == null) {
                throw new IllegalStateException("Compte de trésorerie 531 non trouvé dans le plan comptable");
            }
            if (clientAccount == null) {
                throw new IllegalStateException("Compte clients 411 non trouvé dans le plan comptable");
            }

            // Création de l'écriture comptable (en-tête)
            JournalEntry journalEntry = new JournalEntry();
            journalEntry.setEntryDate(LocalDate.now());
            journalEntry.setDescription("Encaissement LTA " + payment.getLta().getLtaNumber());
            journalEntry.setReference("LTA-PAY-" + payment.getLta().getLtaNumber());
            journalEntry.setFiscalYear(fiscalYear);
            journalEntry.setSourceType(SourceType.LTA_PAYMENT);
            journalEntry.setSourceId(payment.getId());

            // Ligne 1: Débit compte de trésorerie (entrée d'argent)
            journalEntry.addAccountingEntry(
                    treasuryAccount,
                    payment.getAmount(), // débit
                    BigDecimal.ZERO, // crédit
                    "Encaissement LTA " + payment.getLta().getLtaNumber());

            // Ligne 2: Crédit compte clients (diminution créance)
            journalEntry.addAccountingEntry(
                    clientAccount,
                    BigDecimal.ZERO, // débit
                    payment.getAmount(), // crédit
                    "Paiement client LTA " + payment.getLta().getLtaNumber());

            // Sauvegarde de l'écriture comptable
            journalEntryService.saveJournalEntry(journalEntry);

        } catch (Exception e) {
            // Log l'erreur mais ne fait pas échouer le paiement
            LOGGER.severe("Erreur lors de l'intégration comptable du paiement LTA: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Génère une référence comptable unique
     * 
     * @param ltaId ID de la LTA
     * @return Référence comptable
     */
    private String generateAccountingReference(Long ltaId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timeStr = String.valueOf(System.currentTimeMillis()).substring(8); // Derniers 5 chiffres
        return String.format("PAY-%s-%s-%d", dateStr, timeStr, ltaId);
    }

    /**
     * Valide si une LTA est éligible au paiement
     * 
     * @param lta LTA à valider
     * @return true si éligible
     */
    public boolean isEligibleForPayment(LTA lta) {
        return lta.getCalculatedCost() != null
                && lta.getCalculatedCost().compareTo(BigDecimal.ZERO) > 0
                && (lta.getStatus().name().equals("CONFIRMED")
                        || lta.getStatus().name().equals("IN_TRANSIT")
                        || lta.getStatus().name().equals("DELIVERED"))
                && (lta.getPaymentMode().name().equals("CASH")
                        || lta.getPaymentMode().name().equals("PORT_DU"));
    }

    /**
     * Récupère le résumé des paiements pour une LTA
     * 
     * @param ltaId ID de la LTA
     * @return Résumé des paiements
     */
    public Map<String, Object> getPaymentSummary(Long ltaId) {
        Map<String, Object> remainingAmountInfo = calculateRemainingAmount(ltaId);

        return Map.of(
                "ltaId", ltaId,
                "totalCost", remainingAmountInfo.get("totalCost"),
                "totalPaid", BigDecimal.ZERO, // Mode simulation
                "remainingAmount", remainingAmountInfo.get("remainingAmount"),
                "payments", List.of(), // Liste vide en mode simulation
                "isFullyPaid", remainingAmountInfo.get("isFullyPaid"));
    }
}
