package com.freightops.service;

import com.freightops.entity.Shipment;
import com.freightops.entity.TrackingEvent;
import com.freightops.enums.ShipmentStatus;
import com.freightops.enums.ServiceType;
import com.freightops.repository.ShipmentRepository;
import com.freightops.repository.TrackingEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShipmentService {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private TrackingEventRepository trackingEventRepository;

    /**
     * Create a new shipment
     */
    public Shipment createShipment(Shipment shipment) {
        // Generate tracking number if not provided
        if (shipment.getTrackingNumber() == null || shipment.getTrackingNumber().isEmpty()) {
            shipment.setTrackingNumber(generateTrackingNumber());
        }

        // Set default status
        if (shipment.getStatus() == null) {
            shipment.setStatus(ShipmentStatus.PENDING);
        }

        // Calculate expected delivery date based on service type
        if (shipment.getExpectedDeliveryDate() == null && shipment.getServiceType() != null) {
            shipment.setExpectedDeliveryDate(calculateExpectedDeliveryDate(shipment.getServiceType()));
        }

        Shipment savedShipment = shipmentRepository.save(shipment);

        // Create initial tracking event
        addTrackingEvent(savedShipment, ShipmentStatus.PENDING, "Envoi créé dans le système");

        return savedShipment;
    }

    /**
     * Update shipment status and create tracking event
     */
    public Shipment updateShipmentStatus(Long shipmentId, ShipmentStatus newStatus, String description,
            String location) {
        Shipment shipment = getShipmentById(shipmentId);
        ShipmentStatus oldStatus = shipment.getStatus();

        shipment.setStatus(newStatus);

        // Update delivery date if delivered
        if (newStatus == ShipmentStatus.DELIVERED && shipment.getActualDeliveryDate() == null) {
            shipment.setActualDeliveryDate(LocalDateTime.now());
        }

        Shipment updatedShipment = shipmentRepository.save(shipment);

        // Create tracking event
        TrackingEvent event = new TrackingEvent(updatedShipment, newStatus, description);
        event.setLocation(location);
        trackingEventRepository.save(event);

        return updatedShipment;
    }

    /**
     * Get shipment by ID
     */
    @Transactional(readOnly = true)
    public Shipment getShipmentById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment not found with id: " + id));
    }

    /**
     * Get shipment by tracking number
     */
    @Transactional(readOnly = true)
    public Shipment getShipmentByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Shipment not found with tracking number: " + trackingNumber));
    }

    /**
     * Get shipment with tracking events
     */
    @Transactional(readOnly = true)
    public Shipment getShipmentWithEvents(String trackingNumber) {
        Shipment shipment = getShipmentByTrackingNumber(trackingNumber);
        List<TrackingEvent> events = trackingEventRepository.findByShipmentIdOrderByEventDateDesc(shipment.getId());
        shipment.setTrackingEvents(events);
        return shipment;
    }

    /**
     * Search shipments with pagination
     */
    @Transactional(readOnly = true)
    public Page<Shipment> searchShipments(String trackingNumber, String senderName, String recipientName,
            ShipmentStatus status, Long clientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return shipmentRepository.searchShipments(trackingNumber, senderName, recipientName, status, clientId,
                pageable);
    }

    /**
     * Get shipments by client
     */
    @Transactional(readOnly = true)
    public Page<Shipment> getShipmentsByClient(Long clientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return shipmentRepository.findByClientId(clientId, pageable);
    }

    /**
     * Get shipments by status
     */
    @Transactional(readOnly = true)
    public Page<Shipment> getShipmentsByStatus(ShipmentStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return shipmentRepository.findByStatus(status, pageable);
    }

    /**
     * Get in-transit shipments
     */
    @Transactional(readOnly = true)
    public Page<Shipment> getInTransitShipments(int page, int size) {
        List<ShipmentStatus> transitStatuses = Arrays.asList(
                ShipmentStatus.PICKUP_SCHEDULED,
                ShipmentStatus.PICKED_UP,
                ShipmentStatus.IN_TRANSIT,
                ShipmentStatus.OUT_FOR_DELIVERY);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return shipmentRepository.findInTransitShipments(transitStatuses, pageable);
    }

    /**
     * Get overdue shipments
     */
    @Transactional(readOnly = true)
    public List<Shipment> getOverdueShipments() {
        List<ShipmentStatus> finalStatuses = Arrays.asList(
                ShipmentStatus.DELIVERED,
                ShipmentStatus.RETURNED_TO_SENDER,
                ShipmentStatus.CANCELLED,
                ShipmentStatus.LOST,
                ShipmentStatus.DAMAGED);
        return shipmentRepository.findOverdueShipments(LocalDateTime.now(), finalStatuses);
    }

    /**
     * Get shipment statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getShipmentStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Count by status
        List<Object[]> statusCounts = shipmentRepository.countByStatus();
        Map<String, Long> statusMap = statusCounts.stream()
                .collect(Collectors.toMap(
                        row -> ((ShipmentStatus) row[0]).name(),
                        row -> (Long) row[1]));
        stats.put("byStatus", statusMap);

        // Count by client
        List<Object[]> clientCounts = shipmentRepository.countByClient();
        Map<Long, Long> clientMap = clientCounts.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]));
        stats.put("byClient", clientMap);

        // Total counts
        stats.put("total", shipmentRepository.count());
        stats.put("inTransit", getInTransitShipments(0, Integer.MAX_VALUE).getTotalElements());
        stats.put("overdue", getOverdueShipments().size());

        return stats;
    }

    /**
     * Add tracking event to shipment
     */
    public TrackingEvent addTrackingEvent(Shipment shipment, ShipmentStatus status, String description) {
        TrackingEvent event = new TrackingEvent(shipment, status, description);
        return trackingEventRepository.save(event);
    }

    /**
     * Add detailed tracking event
     */
    public TrackingEvent addTrackingEvent(Long shipmentId, ShipmentStatus status, String description,
            String location, String city, String country, String operatorName) {
        Shipment shipment = getShipmentById(shipmentId);

        TrackingEvent event = new TrackingEvent(shipment, status, description);
        event.setLocation(location);
        event.setCity(city);
        event.setCountry(country);
        event.setOperatorName(operatorName);

        return trackingEventRepository.save(event);
    }

    /**
     * Get tracking events for shipment
     */
    @Transactional(readOnly = true)
    public List<TrackingEvent> getTrackingEvents(Long shipmentId) {
        return trackingEventRepository.findByShipmentIdOrderByEventDateDesc(shipmentId);
    }

    /**
     * Delete shipment (soft delete by setting status to CANCELLED)
     */
    public void deleteShipment(Long shipmentId) {
        Shipment shipment = getShipmentById(shipmentId);
        updateShipmentStatus(shipmentId, ShipmentStatus.CANCELLED, "Envoi annulé", null);
    }

    /**
     * Generate unique tracking number
     */
    private String generateTrackingNumber() {
        String prefix = "FO";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf((int) (Math.random() * 1000));
        String trackingNumber = prefix + timestamp.substring(timestamp.length() - 8) + random;

        // Ensure uniqueness
        while (shipmentRepository.existsByTrackingNumber(trackingNumber)) {
            random = String.valueOf((int) (Math.random() * 1000));
            trackingNumber = prefix + timestamp.substring(timestamp.length() - 8) + random;
        }

        return trackingNumber;
    }

    /**
     * Calculate expected delivery date based on service type
     */
    private LocalDateTime calculateExpectedDeliveryDate(ServiceType serviceType) {
        LocalDateTime now = LocalDateTime.now();
        return now.plusDays(serviceType.getMaxDeliveryDays());
    }

    /**
     * Update shipment
     */
    public Shipment updateShipment(Shipment shipment) {
        return shipmentRepository.save(shipment);
    }

    /**
     * Get recent shipments for client
     */
    @Transactional(readOnly = true)
    public List<Shipment> getRecentShipmentsByClient(Long clientId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return shipmentRepository.findRecentShipmentsByClient(clientId, pageable);
    }
}
