package com.freightops.accounting.service;

import com.freightops.accounting.entity.FiscalYear;
import com.freightops.accounting.repository.FiscalYearRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des exercices comptables
 * Gère la création automatique et la validation des exercices
 */
@Service
@Transactional
public class FiscalYearService {

    @Autowired
    private FiscalYearRepository fiscalYearRepository;

    /**
     * Récupère l'exercice comptable actuel
     */
    public FiscalYear getCurrentFiscalYear() {
        LocalDate today = LocalDate.now();
        return fiscalYearRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today);
    }

    /**
     * Récupère un exercice par son année
     */
    public FiscalYear getFiscalYearByYear(Integer year) {
        return fiscalYearRepository.findByYearNumber(year);
    }

    /**
     * Récupère tous les exercices comptables
     */
    public List<FiscalYear> getAllFiscalYears() {
        return fiscalYearRepository.findAllByOrderByYearNumberDesc();
    }

    /**
     * Crée un nouvel exercice comptable
     */
    public FiscalYear createFiscalYear(Integer yearNumber, LocalDate startDate, LocalDate endDate) {
        // Vérification de l'unicité de l'année
        if (fiscalYearRepository.findByYearNumber(yearNumber) != null) {
            throw new IllegalArgumentException("L'exercice " + yearNumber + " existe déjà");
        }

        // Validation des dates
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
        }

        // Vérification des chevauchements avec d'autres exercices
        List<FiscalYear> overlappingYears = fiscalYearRepository.findOverlappingFiscalYears(startDate, endDate);
        if (!overlappingYears.isEmpty()) {
            throw new IllegalArgumentException("Les dates chevauchent avec un exercice existant");
        }

        FiscalYear fiscalYear = new FiscalYear(yearNumber, startDate, endDate);
        return fiscalYearRepository.save(fiscalYear);
    }

    /**
     * Crée automatiquement l'exercice comptable actuel si nécessaire
     */
    public FiscalYear createCurrentFiscalYearIfNotExists() {
        FiscalYear currentFiscalYear = getCurrentFiscalYear();
        if (currentFiscalYear != null) {
            return currentFiscalYear;
        }

        // Création de l'exercice pour l'année en cours
        int currentYear = LocalDate.now().getYear();
        LocalDate startDate = LocalDate.of(currentYear, 1, 1);
        LocalDate endDate = LocalDate.of(currentYear, 12, 31);

        return createFiscalYear(currentYear, startDate, endDate);
    }

    /**
     * Clôture un exercice comptable
     */
    public FiscalYear closeFiscalYear(Long fiscalYearId) {
        Optional<FiscalYear> optionalFiscalYear = fiscalYearRepository.findById(fiscalYearId);
        if (optionalFiscalYear.isEmpty()) {
            throw new IllegalArgumentException("Exercice comptable non trouvé");
        }

        FiscalYear fiscalYear = optionalFiscalYear.get();

        // Vérifier que l'exercice n'est pas déjà clôturé
        if (fiscalYear.getIsClosed()) {
            throw new IllegalStateException("L'exercice est déjà clôturé");
        }

        // Vérifier que l'exercice est terminé
        if (fiscalYear.getEndDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException("Impossible de clôturer un exercice non terminé");
        }

        fiscalYear.setIsClosed(true);
        return fiscalYearRepository.save(fiscalYear);
    }

    /**
     * Réouvre un exercice comptable clôturé
     */
    public FiscalYear reopenFiscalYear(Long fiscalYearId) {
        Optional<FiscalYear> optionalFiscalYear = fiscalYearRepository.findById(fiscalYearId);
        if (optionalFiscalYear.isEmpty()) {
            throw new IllegalArgumentException("Exercice comptable non trouvé");
        }

        FiscalYear fiscalYear = optionalFiscalYear.get();

        if (!fiscalYear.getIsClosed()) {
            throw new IllegalStateException("L'exercice n'est pas clôturé");
        }

        fiscalYear.setIsClosed(false);
        return fiscalYearRepository.save(fiscalYear);
    }

    /**
     * Vérifie si une date appartient à un exercice ouvert
     */
    public boolean isDateInOpenFiscalYear(LocalDate date) {
        FiscalYear fiscalYear = fiscalYearRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date);
        return fiscalYear != null && !fiscalYear.getIsClosed();
    }

    /**
     * Initialise l'exercice comptable de base
     */
    @Transactional
    public void initializeBasicFiscalYear() {
        // Vérifier si des exercices existent déjà
        if (fiscalYearRepository.count() > 0) {
            return; // Exercices déjà initialisés
        }

        // Créer l'exercice pour l'année en cours
        createCurrentFiscalYearIfNotExists();
    }
}
