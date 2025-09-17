package com.freightops.repository;

import com.freightops.entity.TrackingEvent;
import com.freightops.enums.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {

    /**
     * Find tracking events by shipment ID ordered by event date
     */
    List<TrackingEvent> findByShipmentIdOrderByEventDateDesc(Long shipmentId);

    /**
     * Find tracking events by shipment ID and status
     */
    List<TrackingEvent> findByShipmentIdAndStatus(Long shipmentId, ShipmentStatus status);

    /**
     * Find latest tracking event for a shipment
     */
    @Query("SELECT te FROM TrackingEvent te WHERE te.shipment.id = :shipmentId ORDER BY te.eventDate DESC")
    List<TrackingEvent> findLatestByShipmentId(@Param("shipmentId") Long shipmentId, Pageable pageable);

    /**
     * Find tracking events by date range
     */
    @Query("SELECT te FROM TrackingEvent te WHERE te.eventDate BETWEEN :startDate AND :endDate ORDER BY te.eventDate DESC")
    Page<TrackingEvent> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Find tracking events by location
     */
    List<TrackingEvent> findByLocationContainingIgnoreCase(String location);

    /**
     * Find tracking events by city
     */
    List<TrackingEvent> findByCityContainingIgnoreCase(String city);

    /**
     * Find tracking events by operator
     */
    List<TrackingEvent> findByOperatorId(Long operatorId);

    /**
     * Count events by status
     */
    @Query("SELECT te.status, COUNT(te) FROM TrackingEvent te GROUP BY te.status")
    List<Object[]> countByStatus();

    /**
     * Find exception events
     */
    @Query("SELECT te FROM TrackingEvent te WHERE te.exceptionCode IS NOT NULL ORDER BY te.eventDate DESC")
    Page<TrackingEvent> findExceptionEvents(Pageable pageable);

    /**
     * Find delivery events with signature
     */
    @Query("SELECT te FROM TrackingEvent te WHERE te.status = :deliveredStatus AND te.signatureName IS NOT NULL")
    List<TrackingEvent> findDeliveryEventsWithSignature(@Param("deliveredStatus") ShipmentStatus deliveredStatus);

    /**
     * Find events by facility
     */
    List<TrackingEvent> findByFacilityCodeOrderByEventDateDesc(String facilityCode);
}
