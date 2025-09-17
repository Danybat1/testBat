package com.freightops.repository;

import com.freightops.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

        /**
         * Trouver le taux de change actuel entre deux devises
         */
        @Query("SELECT er FROM ExchangeRate er WHERE er.fromCurrency.code = :fromCode " +
                        "AND er.toCurrency.code = :toCode AND er.isActive = true " +
                        "ORDER BY er.effectiveDate DESC")
        Optional<ExchangeRate> findCurrentRate(@Param("fromCode") String fromCode,
                        @Param("toCode") String toCode);

        /**
         * Trouver tous les taux actifs pour une devise donnée
         */
        @Query("SELECT er FROM ExchangeRate er WHERE " +
                        "(er.fromCurrency.code = :currencyCode OR er.toCurrency.code = :currencyCode) " +
                        "AND er.isActive = true ORDER BY er.effectiveDate DESC")
        List<ExchangeRate> findActiveByCurrency(@Param("currencyCode") String currencyCode);

        /**
         * Trouver tous les taux de change actifs
         */
        @Query("SELECT er FROM ExchangeRate er " +
                        "JOIN FETCH er.fromCurrency " +
                        "JOIN FETCH er.toCurrency " +
                        "WHERE er.isActive = true " +
                        "ORDER BY er.effectiveDate DESC")
        List<ExchangeRate> findByIsActiveTrueOrderByEffectiveDateDesc();

        /**
         * Trouver l'historique des taux entre deux devises
         */
        @Query("SELECT er FROM ExchangeRate er WHERE er.fromCurrency.code = :fromCode " +
                        "AND er.toCurrency.code = :toCode ORDER BY er.effectiveDate DESC")
        List<ExchangeRate> findRateHistory(@Param("fromCode") String fromCode,
                        @Param("toCode") String toCode);

        /**
         * Vérifier si un taux existe déjà pour une paire de devises
         */
        boolean existsByFromCurrency_CodeAndToCurrency_CodeAndIsActiveTrue(String fromCode, String toCode);

        /**
         * Trouver les taux mis à jour après une date donnée
         */
        @Query("SELECT er FROM ExchangeRate er WHERE er.updatedAt > :since " +
                        "AND er.isActive = true ORDER BY er.updatedAt DESC")
        List<ExchangeRate> findUpdatedSince(@Param("since") LocalDateTime since);
}
