package com.freightops.repository;

import com.freightops.entity.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Tariff entity operations
 */
@Repository
public interface TariffRepository extends JpaRepository<Tariff, Long> {

    /**
     * Find tariff by origin and destination cities
     */
    @Query("SELECT t FROM Tariff t WHERE t.originCity.id = :originId AND t.destinationCity.id = :destinationId " +
           "AND t.isActive = true AND (t.effectiveFrom IS NULL OR t.effectiveFrom <= :now) " +
           "AND (t.effectiveUntil IS NULL OR t.effectiveUntil >= :now)")
    Optional<Tariff> findByOriginAndDestination(@Param("originId") Long originId, 
                                               @Param("destinationId") Long destinationId,
                                               @Param("now") LocalDateTime now);

    /**
     * Find all active tariffs
     */
    @Query("SELECT t FROM Tariff t WHERE t.isActive = true " +
           "AND (t.effectiveFrom IS NULL OR t.effectiveFrom <= :now) " +
           "AND (t.effectiveUntil IS NULL OR t.effectiveUntil >= :now)")
    List<Tariff> findActiveTariffs(@Param("now") LocalDateTime now);

    /**
     * Find tariffs by origin city
     */
    @Query("SELECT t FROM Tariff t WHERE t.originCity.id = :originId AND t.isActive = true")
    List<Tariff> findByOriginCity(@Param("originId") Long originId);

    /**
     * Find tariffs by destination city
     */
    @Query("SELECT t FROM Tariff t WHERE t.destinationCity.id = :destinationId AND t.isActive = true")
    List<Tariff> findByDestinationCity(@Param("destinationId") Long destinationId);

    /**
     * Check if tariff exists for route (excluding specific ID for updates)
     */
    boolean existsByOriginCityIdAndDestinationCityIdAndIdNot(Long originId, Long destinationId, Long id);

    /**
     * Check if tariff exists for route
     */
    boolean existsByOriginCityIdAndDestinationCityId(Long originId, Long destinationId);


    /**
     * Find expired tariffs
     */
    @Query("SELECT t FROM Tariff t WHERE t.effectiveUntil IS NOT NULL AND t.effectiveUntil < :now")
    List<Tariff> findExpiredTariffs(@Param("now") LocalDateTime now);
}
