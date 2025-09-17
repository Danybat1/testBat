package com.freightops.repository;

import com.freightops.entity.Shipment;
import com.freightops.enums.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    /**
     * Find shipment by tracking number
     */
    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    /**
     * Find shipments by customer reference
     */
    List<Shipment> findByCustomerReference(String customerReference);

    /**
     * Find shipments by reference number
     */
    List<Shipment> findByReferenceNumber(String referenceNumber);

    /**
     * Find shipments by client ID
     */
    Page<Shipment> findByClientId(Long clientId, Pageable pageable);

    /**
     * Find shipments by status
     */
    Page<Shipment> findByStatus(ShipmentStatus status, Pageable pageable);

    /**
     * Find shipments by multiple statuses
     */
    Page<Shipment> findByStatusIn(List<ShipmentStatus> statuses, Pageable pageable);

    /**
     * Find shipments by sender email
     */
    List<Shipment> findBySenderEmail(String senderEmail);

    /**
     * Find shipments by recipient email
     */
    List<Shipment> findByRecipientEmail(String recipientEmail);

    /**
     * Find shipments by date range
     */
    @Query("SELECT s FROM Shipment s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    Page<Shipment> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Find shipments by pickup date range
     */
    @Query("SELECT s FROM Shipment s WHERE s.pickupDate BETWEEN :startDate AND :endDate")
    Page<Shipment> findByPickupDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Find shipments by expected delivery date range
     */
    @Query("SELECT s FROM Shipment s WHERE s.expectedDeliveryDate BETWEEN :startDate AND :endDate")
    Page<Shipment> findByExpectedDeliveryDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Search shipments by multiple criteria
     */
    @Query("SELECT s FROM Shipment s WHERE " +
            "(:trackingNumber IS NULL OR s.trackingNumber LIKE %:trackingNumber%) AND " +
            "(:senderName IS NULL OR s.senderName LIKE %:senderName%) AND " +
            "(:recipientName IS NULL OR s.recipientName LIKE %:recipientName%) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:clientId IS NULL OR s.clientId = :clientId)")
    Page<Shipment> searchShipments(@Param("trackingNumber") String trackingNumber,
            @Param("senderName") String senderName,
            @Param("recipientName") String recipientName,
            @Param("status") ShipmentStatus status,
            @Param("clientId") Long clientId,
            Pageable pageable);

    /**
     * Count shipments by status
     */
    @Query("SELECT s.status, COUNT(s) FROM Shipment s GROUP BY s.status")
    List<Object[]> countByStatus();

    /**
     * Count shipments by client
     */
    @Query("SELECT s.clientId, COUNT(s) FROM Shipment s WHERE s.clientId IS NOT NULL GROUP BY s.clientId")
    List<Object[]> countByClient();

    /**
     * Find overdue shipments (expected delivery date passed but not delivered)
     */
    @Query("SELECT s FROM Shipment s WHERE s.expectedDeliveryDate < :currentDate AND s.status NOT IN :finalStatuses")
    List<Shipment> findOverdueShipments(@Param("currentDate") LocalDateTime currentDate,
            @Param("finalStatuses") List<ShipmentStatus> finalStatuses);

    /**
     * Find shipments in transit
     */
    @Query("SELECT s FROM Shipment s WHERE s.status IN :transitStatuses")
    Page<Shipment> findInTransitShipments(@Param("transitStatuses") List<ShipmentStatus> transitStatuses,
            Pageable pageable);

    /**
     * Find recent shipments for a client
     */
    @Query("SELECT s FROM Shipment s WHERE s.clientId = :clientId ORDER BY s.createdAt DESC")
    List<Shipment> findRecentShipmentsByClient(@Param("clientId") Long clientId, Pageable pageable);

    /**
     * Check if tracking number exists
     */
    boolean existsByTrackingNumber(String trackingNumber);
}
