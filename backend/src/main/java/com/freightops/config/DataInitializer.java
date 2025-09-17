package com.freightops.config;

import com.freightops.entity.City;
import com.freightops.entity.CashBox;
import com.freightops.repository.CityRepository;
import com.freightops.repository.CashBoxRepository;
import com.freightops.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Initializer for DRC Cities, Default Cash Box, and Currency System
 * Populates the database with DRC cities, creates a default cash box, and
 * initializes currencies on application startup
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final CityRepository cityRepository;
    private final CashBoxRepository cashBoxRepository;
    private final CurrencyService currencyService;

    @Autowired
    public DataInitializer(CityRepository cityRepository, CashBoxRepository cashBoxRepository,
            CurrencyService currencyService) {
        this.cityRepository = cityRepository;
        this.cashBoxRepository = cashBoxRepository;
        this.currencyService = currencyService;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("🚀 Initialisation des données de base...");
        initializeDRCCities();
        initializeDefaultCashBox();
        initializeCurrencySystem();
        logger.info("✅ Initialisation des données terminée");
    }

    private void initializeDRCCities() {
        // Check if cities already exist to avoid duplicates
        if (cityRepository.count() > 0) {
            return;
        }

        // DRC Cities with IATA codes - Extended list
        City[] drcCities = {
                // Principales villes
                new City("Kinshasa", "FIH", "République Démocratique du Congo"),
                new City("Lubumbashi", "FBM", "République Démocratique du Congo"),
                new City("Kisangani", "FKI", "République Démocratique du Congo"),
                new City("Mbuji-Mayi", "MJM", "République Démocratique du Congo"),
                new City("Goma", "GOM", "République Démocratique du Congo"),
                new City("Kananga", "KGA", "République Démocratique du Congo"),
                new City("Bukavu", "BKY", "République Démocratique du Congo"),
                new City("Kolwezi", "KWZ", "République Démocratique du Congo"),
                new City("Mbandaka", "MDK", "République Démocratique du Congo"),
                new City("Matadi", "MAT", "République Démocratique du Congo"),

                // Villes importantes
                new City("Kindu", "KND", "République Démocratique du Congo"),
                new City("Gemena", "GMA", "République Démocratique du Congo"),
                new City("Isiro", "IRP", "République Démocratique du Congo"),
                new City("Bunia", "BUX", "République Démocratique du Congo"),
                new City("Bandundu", "BAN", "République Démocratique du Congo"),
                new City("Tshikapa", "TSH", "République Démocratique du Congo"),
                new City("Uvira", "UVR", "République Démocratique du Congo"),
                new City("Boma", "BOA", "République Démocratique du Congo"),
                new City("Kamina", "KMN", "République Démocratique du Congo"),
                new City("Lodja", "LJA", "République Démocratique du Congo"),
                new City("Ilebo", "PFR", "République Démocratique du Congo"),
                new City("Gbadolite", "BDT", "République Démocratique du Congo"),
                new City("Kalemie", "FMI", "République Démocratique du Congo"),
                new City("Lisala", "LIQ", "République Démocratique du Congo"),
                new City("Inongo", "INO", "République Démocratique du Congo"),

                // Villes supplémentaires
                new City("Kikwit", "KKW", "République Démocratique du Congo"),
                new City("Likasi", "LIK", "République Démocratique du Congo"),
                new City("Mwene-Ditu", "MWX", "République Démocratique du Congo"),
                new City("Kabinda", "KAB", "République Démocratique du Congo"),
                new City("Tshela", "TSE", "République Démocratique du Congo"),
                new City("Kenge", "KEN", "République Démocratique du Congo"),
                new City("Kasongo", "KAS", "République Démocratique du Congo"),
                new City("Pweto", "PWO", "République Démocratique du Congo"),
                new City("Manono", "MNO", "République Démocratique du Congo"),
                new City("Shabunda", "SHB", "République Démocratique du Congo"),
                new City("Baraka", "BAR", "République Démocratique du Congo"),
                new City("Fizi", "FIZ", "République Démocratique du Congo"),
                new City("Rutshuru", "RUT", "République Démocratique du Congo"),
                new City("Beni", "BEN", "République Démocratique du Congo"),
                new City("Buta", "BUT", "République Démocratique du Congo"),
                new City("Bumba", "BUM", "République Démocratique du Congo")
        };

        // Save all cities
        for (City city : drcCities) {
            try {
                cityRepository.save(city);
            } catch (Exception e) {
                logger.error("Error initializing city " + city.getName() + ": " + e.getMessage(), e);
            }
        }
    }

    private void initializeDefaultCashBox() {
        try {
            // Vérifier s'il existe déjà une caisse active
            List<CashBox> existingCashBoxes = cashBoxRepository.findByActiveTrue();

            if (existingCashBoxes.isEmpty()) {
                logger.info("Aucune caisse active trouvée. Création de la caisse par défaut...");

                // Créer une caisse par défaut
                CashBox defaultCashBox = new CashBox();
                defaultCashBox.setName("Caisse Principale");
                defaultCashBox.setDescription("Caisse principale pour les opérations de trésorerie");
                defaultCashBox.setInitialBalance(new BigDecimal("1000.00"));
                defaultCashBox.setCurrentBalance(new BigDecimal("1000.00"));
                defaultCashBox.setActive(true);

                CashBox savedCashBox = cashBoxRepository.save(defaultCashBox);
                logger.info("✅ Caisse créée automatiquement: {} (ID: {})", savedCashBox.getName(),
                        savedCashBox.getId());
            } else {
                logger.info("✅ Caisse active existante: {} (ID: {})", existingCashBoxes.get(0).getName(),
                        existingCashBoxes.get(0).getId());
            }
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'initialisation de la caisse par défaut: {}", e.getMessage(), e);
        }
    }

    /**
     * Initialiser le système de devises avec USD et CDF
     */
    private void initializeCurrencySystem() {
        try {
            logger.info("🔄 Initialisation du système de devises...");
            currencyService.initializeDefaultData();
            logger.info("✅ Système de devises initialisé avec succès");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'initialisation du système de devises: {}", e.getMessage(), e);
        }
    }
}
