package com.freightops.accounting.repository;

import com.freightops.accounting.entity.FiscalYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository pour la gestion des exercices comptables
 */
@Repository
public interface FiscalYearRepository extends JpaRepository<FiscalYear, Long> {

    /**
     * Trouve un exercice par son année
     */
    FiscalYear findByYearNumber(Integer yearNumber);

    /**
     * Trouve tous les exercices triés par année décroissante
     */
    List<FiscalYear> findAllByOrderByYearNumberDesc();

    /**
     * Trouve l'exercice comptable contenant une date donnée
     */
    FiscalYear findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate date1, LocalDate date2);

    /**
     * Trouve les exercices ouverts (non clôturés)
     */
    List<FiscalYear> findByIsClosedFalseOrderByYearNumberDesc();

    /**
     * Trouve les exercices clôturés
     */
    List<FiscalYear> findByIsClosedTrueOrderByYearNumberDesc();

    /**
     * Vérifie les chevauchements de dates avec d'autres exercices
     */
    @Query("SELECT fy FROM FiscalYear fy WHERE " +
            "(fy.startDate <= :endDate AND fy.endDate >= :startDate)")
    List<FiscalYear> findOverlappingFiscalYears(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Compte le nombre total d'exercices
     */
    long count();

    /**
     * Trouve l'exercice le plus récent
     */
    @Query("SELECT fy FROM FiscalYear fy ORDER BY fy.yearNumber DESC LIMIT 1")
    FiscalYear findLatestFiscalYear();

    /**
     * Trouve les exercices par période
     */
    List<FiscalYear> findByYearNumberBetweenOrderByYearNumber(Integer startYear, Integer endYear);
}
