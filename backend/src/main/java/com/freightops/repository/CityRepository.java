package com.freightops.repository;

import com.freightops.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for City entity operations
 */
@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    /**
     * Find city by IATA code
     */
    Optional<City> findByIataCode(String iataCode);
    
    Optional<City> findByIataCodeIgnoreCase(String iataCode);

    /**
     * Find active cities
     */
    List<City> findByIsActiveTrueOrderByName();

    /**
     * Search cities by name or IATA code (for autocomplete)
     */
    @Query("SELECT c FROM City c WHERE c.isActive = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.iataCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<City> searchByNameOrIataCode(@Param("search") String search);
    
    /**
     * Search cities by name or IATA code (alternative method name for compatibility)
     */
    @Query("SELECT c FROM City c WHERE " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.iataCode) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<City> findByNameContainingIgnoreCaseOrIataCodeContainingIgnoreCase(@Param("query") String query, @Param("query") String query2);

    /**
     * Find cities by country
     */
    List<City> findByCountryIgnoreCaseAndIsActiveTrueOrderByName(String country);

    /**
     * Check if IATA code exists (excluding specific ID for updates)
     */
    boolean existsByIataCodeIgnoreCaseAndIdNot(String iataCode, Long id);

    /**
     * Check if IATA code exists
     */
    boolean existsByIataCode(String iataCode);
    
    boolean existsByIataCodeIgnoreCase(String iataCode);
    
    /**
     * Check if name exists (case insensitive)
     */
    boolean existsByNameIgnoreCase(String name);
}
