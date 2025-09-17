package com.freightops.accounting.config;

import com.freightops.accounting.service.AccountService;
import com.freightops.accounting.service.FiscalYearService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * Initialise automatiquement le module comptable au démarrage de l'application
 * Crée le plan comptable de base et l'exercice comptable actuel
 */
@Component
public class AccountingInitializer implements ApplicationRunner {

    private static final Logger logger = Logger.getLogger(AccountingInitializer.class.getName());

    @Autowired
    private AccountService accountService;

    @Autowired
    private FiscalYearService fiscalYearService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Initialisation du module comptable...");

        try {
            // Initialisation de l'exercice comptable
            fiscalYearService.initializeBasicFiscalYear();

            // Initialisation du plan comptable
            accountService.initializeBasicChartOfAccounts();

            logger.info("Module comptable initialisé avec succès");

        } catch (Exception e) {
            logger.severe("Erreur lors de l'initialisation du module comptable: " + e.getMessage());
            throw e;
        }
    }
}
