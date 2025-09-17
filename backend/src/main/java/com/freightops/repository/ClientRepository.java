package com.freightops.repository;

import com.freightops.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Client entity operations
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    /**
     * Find active clients
     */
    List<Client> findByIsActiveTrueOrderByName();

    /**
     * Find clients by name containing (case insensitive)
     */
    List<Client> findByNameContainingIgnoreCase(String name);

    /**
     * Search clients by name or contact number (for autocomplete)
     */
    @Query("SELECT c FROM Client c WHERE c.isActive = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "c.contactNumber LIKE CONCAT('%', :search, '%'))")
    List<Client> searchByNameOrContact(@Param("search") String search);

    /**
     * Find clients by contact person
     */
    List<Client> findByContactPersonIgnoreCaseContainingAndIsActiveTrueOrderByName(String contactPerson);

    /**
     * Find clients with LTA count
     */
    @Query("SELECT c, COUNT(l) as ltaCount FROM Client c LEFT JOIN c.ltas l " +
           "WHERE c.isActive = true GROUP BY c ORDER BY c.name")
    List<Object[]> findClientsWithLtaCount();

    /**
     * Check if client has active LTAs
     */
    @Query("SELECT COUNT(l) > 0 FROM LTA l WHERE l.client.id = :clientId AND l.status != 'CANCELLED'")
    boolean hasActiveLtas(@Param("clientId") Long clientId);
}
