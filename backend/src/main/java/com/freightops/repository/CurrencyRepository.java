package com.freightops.repository;

import com.freightops.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {

    /**
     * Trouver toutes les devises actives
     */
    List<Currency> findByIsActiveTrueOrderByCodeAsc();

    /**
     * Trouver la devise par défaut
     */
    Optional<Currency> findByIsDefaultTrue();

    /**
     * Vérifier si une devise existe et est active
     */
    boolean existsByCodeAndIsActiveTrue(String code);

    /**
     * Trouver une devise active par son code
     */
    Optional<Currency> findByCodeAndIsActiveTrue(String code);

    /**
     * Compter le nombre de devises par défaut (ne devrait pas dépasser 1)
     */
    @Query("SELECT COUNT(c) FROM Currency c WHERE c.isDefault = true")
    long countDefaultCurrencies();
}
