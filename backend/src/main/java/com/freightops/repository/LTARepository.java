package com.freightops.repository;

import com.freightops.entity.LTA;
import com.freightops.enums.LTAStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * LTA Repository Interface
 * Provides data access operations for LTA entities
 */
@Repository
public interface LTARepository extends JpaRepository<LTA, Long> {

        /**
         * Find LTA by LTA number
         * 
         * @param ltaNumber the LTA number
         * @return Optional LTA
         */
        Optional<LTA> findByLtaNumber(String ltaNumber);

        /**
         * Find LTA by tracking number
         * 
         * @param trackingNumber the tracking number
         * @return Optional LTA
         */
        Optional<LTA> findByTrackingNumber(String trackingNumber);

        /**
         * Find LTAs by status
         * 
         * @param status   the LTA status
         * @param pageable pagination information
         * @return Page of LTAs
         */
        Page<LTA> findByStatus(LTAStatus status, Pageable pageable);

        /**
         * Find LTAs by shipper containing (case-insensitive)
         * 
         * @param shipper  the shipper name
         * @param pageable pagination information
         * @return Page of LTAs
         */
        Page<LTA> findByShipperNameContainingIgnoreCase(String shipper, Pageable pageable);

        /**
         * Find LTAs by consignee containing (case-insensitive)
         * 
         * @param consignee the consignee name
         * @param pageable  pagination information
         * @return Page of LTAs
         */
        Page<LTA> findByConsigneeNameContainingIgnoreCase(String consignee, Pageable pageable);

        /**
         * Find LTAs by multiple criteria
         * 
         * @param status    the status (optional)
         * @param shipper   the shipper name (optional)
         * @param consignee the consignee name (optional)
         * @param pageable  pagination information
         * @return Page of LTAs
         */
        @Query("SELECT l FROM LTA l WHERE " +
                        "(:status IS NULL OR l.status = :status) AND " +
                        "(:shipper IS NULL OR LOWER(l.shipperName) LIKE LOWER(CONCAT('%', :shipper, '%'))) AND " +
                        "(:consignee IS NULL OR LOWER(l.consigneeName) LIKE LOWER(CONCAT('%', :consignee, '%')))")
        Page<LTA> findByMultipleCriteria(@Param("status") LTAStatus status,
                        @Param("shipper") String shipper,
                        @Param("consignee") String consignee,
                        Pageable pageable);

        /**
         * Count LTAs by status
         * 
         * @param status the LTA status
         * @return count of LTAs
         */
        long countByStatus(LTAStatus status);

        /**
         * Check if LTA number exists
         * 
         * @param ltaNumber the LTA number
         * @return true if exists
         */
        boolean existsByLtaNumber(String ltaNumber);

        /**
         * Check if tracking number exists
         * 
         * @param trackingNumber the tracking number
         * @return true if exists
         */
        boolean existsByTrackingNumber(String trackingNumber);

        /**
         * Find LTAs eligible for payment
         * LTAs with calculated cost > 0 and payment modes CASH or PORT_DU
         * and status CONFIRMED, IN_TRANSIT, or DELIVERED
         * 
         * @return List of LTAs eligible for payment
         */
        @Query("""
                        SELECT l FROM LTA l
                        LEFT JOIN FETCH l.client
                        WHERE l.calculatedCost IS NOT NULL
                        AND l.calculatedCost > 0
                        AND l.status IN ('CONFIRMED', 'IN_TRANSIT', 'DELIVERED')
                        AND l.paymentMode IN ('CASH', 'PORT_DU')
                        ORDER BY l.createdAt DESC
                        """)
        List<LTA> findLTAsEligibleForPayment();

        /**
         * Find LTAs eligible for payment as DTO for better performance
         * Avoids lazy loading issues and reduces data transfer
         * 
         * @return List of LTAPaymentDTO eligible for payment
         */
        @Query("""
                        SELECT new com.freightops.dto.LTAPaymentDTO(
                            l.id, l.ltaNumber, l.trackingNumber,
                            oc.name, dc.name, oc.iataCode, dc.iataCode,
                            CAST(l.paymentMode AS string), CAST(l.status AS string), l.calculatedCost,
                            c.name, l.shipperName, l.consigneeName,
                            l.createdAt, l.packageCount, l.totalWeight
                        )
                        FROM LTA l
                        LEFT JOIN l.originCity oc
                        LEFT JOIN l.destinationCity dc
                        LEFT JOIN l.client c
                        WHERE l.calculatedCost IS NOT NULL
                        AND l.calculatedCost > 0
                        AND l.status IN ('CONFIRMED', 'IN_TRANSIT', 'DELIVERED')
                        AND l.paymentMode IN ('CASH', 'PORT_DU')
                        ORDER BY l.createdAt DESC
                        """)
        List<com.freightops.dto.LTAPaymentDTO> findLTAsEligibleForPaymentAsDTO();
}
