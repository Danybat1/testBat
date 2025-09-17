package com.freightops.accounting.service;

import com.freightops.accounting.entity.*;
import com.freightops.accounting.enums.AccountType;
import com.freightops.accounting.enums.SourceType;
import com.freightops.accounting.events.*;
import com.freightops.entity.Invoice;
import com.freightops.entity.Payment;
import com.freightops.entity.LTA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.logging.Logger;

/**
 * Service principal pour la gestion comptable automatique
 * Implémente les règles de partie double et génère les écritures
 * automatiquement
 */
@Service
@Transactional
public class AccountingService {

    private static final Logger logger = Logger.getLogger(AccountingService.class.getName());

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private FiscalYearService fiscalYearService;

    /**
     * Génère automatiquement les écritures comptables lors de la création d'une
     * facture
     * Règle : Débit 411 Clients / Crédit 701 Ventes de services
     */
    @EventListener
    public void handleInvoiceCreated(InvoiceCreatedEvent event) {
        try {
            Invoice invoice = event.getInvoice();
            String createdBy = event.getCreatedBy();

            // Récupération de l'exercice comptable actuel
            FiscalYear currentFiscalYear = fiscalYearService.getCurrentFiscalYear();
            if (currentFiscalYear == null) {
                logger.warning("Aucun exercice comptable actuel trouvé");
                return;
            }

            // Création de l'écriture comptable
            JournalEntry journalEntry = new JournalEntry(
                    LocalDate.now(),
                    "Facturation client - " + invoice.getInvoiceNumber(),
                    currentFiscalYear,
                    SourceType.INVOICE,
                    invoice.getId());
            journalEntry.setReference(invoice.getInvoiceNumber());
            journalEntry.setCreatedBy(createdBy);

            // Récupération des comptes
            Account clientAccount = accountService.getAccountByNumber("411");
            Account salesAccount = accountService.getAccountByNumber("701");

            if (clientAccount == null || salesAccount == null) {
                logger.warning("Comptes comptables non trouvés (411 ou 701)");
                return;
            }

            // Ligne 1: Débit 411 Clients
            journalEntry.addAccountingEntry(
                    clientAccount,
                    invoice.getTotalAmount(),
                    BigDecimal.ZERO,
                    "Créance client - " + invoice.getClient().getName());

            // Ligne 2: Crédit 701 Ventes de services
            journalEntry.addAccountingEntry(
                    salesAccount,
                    BigDecimal.ZERO,
                    invoice.getAmountExcludingTax(),
                    "Vente de services - " + getInvoiceTypeLabel(invoice));

            // Si TVA > 0, ajouter ligne TVA
            if (invoice.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
                Account vatAccount = accountService.getAccountByNumber("445");
                if (vatAccount != null) {
                    journalEntry.addAccountingEntry(
                            vatAccount,
                            BigDecimal.ZERO,
                            invoice.getTaxAmount(),
                            "TVA collectée");
                }
            }

            // Sauvegarde de l'écriture
            journalEntryService.saveJournalEntry(journalEntry);

        } catch (Exception e) {
            logger.severe("Erreur lors de la génération de l'écriture comptable pour la facture: " + e.getMessage());
        }
    }

    /**
     * Génère automatiquement les écritures comptables lors d'un paiement
     * Règle : Débit 512 Banque ou 531 Caisse / Crédit 411 Clients
     */
    @EventListener
    public void handlePaymentReceived(PaymentReceivedEvent event) {
        try {
            Payment payment = event.getPayment();
            String createdBy = event.getCreatedBy();

            FiscalYear currentFiscalYear = fiscalYearService.getCurrentFiscalYear();
            if (currentFiscalYear == null) {
                logger.warning("Aucun exercice comptable actuel trouvé");
                return;
            }

            // Création de l'écriture comptable
            JournalEntry journalEntry = new JournalEntry(
                    LocalDate.now(),
                    "Encaissement client - " + payment.getPaymentMethod(),
                    currentFiscalYear,
                    SourceType.PAYMENT,
                    payment.getId());
            journalEntry.setCreatedBy(createdBy);

            // Détermination du compte de trésorerie selon le mode de paiement
            Account treasuryAccount = getTreasuryAccountByPaymentMethod(payment.getPaymentMethod());
            Account clientAccount = accountService.getAccountByNumber("411");

            if (treasuryAccount == null || clientAccount == null) {
                logger.warning("Comptes comptables non trouvés pour le paiement");
                return;
            }

            // Ligne 1: Débit compte de trésorerie
            journalEntry.addAccountingEntry(
                    treasuryAccount,
                    payment.getAmount(),
                    BigDecimal.ZERO,
                    "Encaissement " + payment.getPaymentMethod());

            // Ligne 2: Crédit 411 Clients
            journalEntry.addAccountingEntry(
                    clientAccount,
                    BigDecimal.ZERO,
                    payment.getAmount(),
                    "Règlement facture " + payment.getInvoice().getInvoiceNumber());

            // Sauvegarde de l'écriture
            journalEntryService.saveJournalEntry(journalEntry);

        } catch (Exception e) {
            logger.severe("Erreur lors de la génération de l'écriture comptable pour le paiement: " + e.getMessage());
        }
    }

    /**
     * Génère automatiquement les écritures comptables lors de la finalisation d'une
     * LTA
     * Règle : Débit 411 Clients / Crédit 701 Ventes de transport
     */
    @EventListener
    public void handleLTACompleted(LTACompletedEvent event) {
        try {
            LTA lta = event.getLta();
            String createdBy = event.getCreatedBy();

            FiscalYear currentFiscalYear = fiscalYearService.getCurrentFiscalYear();
            if (currentFiscalYear == null) {
                logger.warning("Aucun exercice comptable actuel trouvé");
                return;
            }

            // Création de l'écriture comptable
            JournalEntry journalEntry = new JournalEntry(
                    LocalDate.now(),
                    "Transport aérien - " + lta.getLtaNumber(),
                    currentFiscalYear,
                    SourceType.LTA,
                    lta.getId());
            journalEntry.setReference(lta.getLtaNumber());
            journalEntry.setCreatedBy(createdBy);

            // Récupération des comptes
            Account clientAccount = accountService.getAccountByNumber("411");
            Account transportAccount = accountService.getAccountByNumber("701");

            if (clientAccount == null || transportAccount == null) {
                logger.warning("Comptes comptables non trouvés pour la LTA");
                return;
            }

            // Ligne 1: Débit 411 Clients
            journalEntry.addAccountingEntry(
                    clientAccount,
                    lta.getCalculatedCost(),
                    BigDecimal.ZERO,
                    "Transport aérien - " + lta.getClient().getName());

            // Ligne 2: Crédit 701 Ventes de transport
            journalEntry.addAccountingEntry(
                    transportAccount,
                    BigDecimal.ZERO,
                    lta.getCalculatedCost(),
                    "Prestation transport aérien");

            // Sauvegarde de l'écriture
            journalEntryService.saveJournalEntry(journalEntry);

        } catch (Exception e) {
            logger.severe("Erreur lors de la génération de l'écriture comptable pour la LTA: " + e.getMessage());
        }
    }

    /**
     * Détermine le compte de trésorerie selon le mode de paiement
     */
    private Account getTreasuryAccountByPaymentMethod(String paymentMethod) {
        switch (paymentMethod.toUpperCase()) {
            case "CASH":
            case "ESPECES":
                return accountService.getAccountByNumber("531"); // Caisse
            case "BANK_TRANSFER":
            case "VIREMENT":
            case "CHEQUE":
            default:
                return accountService.getAccountByNumber("512"); // Banque
        }
    }

    /**
     * Retourne le libellé du type de facture
     */
    private String getInvoiceTypeLabel(Invoice invoice) {
        switch (invoice.getType()) {
            case TRANSPORT:
                return "Transport de marchandises";
            case PASSENGER:
                return "Transport de passagers";
            case ADDITIONAL_SERVICES:
                return "Services additionnels";
            default:
                return "Prestation de services";
        }
    }
}
