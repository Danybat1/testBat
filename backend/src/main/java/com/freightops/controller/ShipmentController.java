package com.freightops.controller;

import com.freightops.entity.Shipment;
import com.freightops.entity.TrackingEvent;
import com.freightops.enums.ShipmentStatus;
import com.freightops.service.ShipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shipments")
@CrossOrigin(origins = "*")
public class ShipmentController {

    @Autowired
    private ShipmentService shipmentService;

    /**
     * Create a new shipment
     */
    @PostMapping
    public ResponseEntity<Shipment> createShipment(@RequestBody Shipment shipment) {
        try {
            Shipment createdShipment = shipmentService.createShipment(shipment);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdShipment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Get shipment by tracking number (Public endpoint for client portal)
     */
    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<Shipment> trackShipment(@PathVariable String trackingNumber) {
        try {
            Shipment shipment = shipmentService.getShipmentWithEvents(trackingNumber);
            return ResponseEntity.ok(shipment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get shipment by ID (Admin only)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Shipment> getShipmentById(@PathVariable Long id) {
        try {
            Shipment shipment = shipmentService.getShipmentById(id);
            return ResponseEntity.ok(shipment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Search shipments with filters (Admin only)
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Shipment>> searchShipments(
            @RequestParam(required = false) String trackingNumber,
            @RequestParam(required = false) String senderName,
            @RequestParam(required = false) String recipientName,
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(required = false) Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Shipment> shipments = shipmentService.searchShipments(
                trackingNumber, senderName, recipientName, status, clientId, page, size);
        return ResponseEntity.ok(shipments);
    }

    /**
     * Get shipments by client (Client portal)
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<Page<Shipment>> getShipmentsByClient(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Shipment> shipments = shipmentService.getShipmentsByClient(clientId, page, size);
        return ResponseEntity.ok(shipments);
    }

    /**
     * Get shipments by status (Admin only)
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<Shipment>> getShipmentsByStatus(
            @PathVariable ShipmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Shipment> shipments = shipmentService.getShipmentsByStatus(status, page, size);
        return ResponseEntity.ok(shipments);
    }

    /**
     * Get in-transit shipments (Admin only)
     */
    @GetMapping("/in-transit")
    public ResponseEntity<Page<Shipment>> getInTransitShipments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Shipment> shipments = shipmentService.getInTransitShipments(page, size);
        return ResponseEntity.ok(shipments);
    }

    /**
     * Get overdue shipments (Admin only)
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<Shipment>> getOverdueShipments() {
        List<Shipment> shipments = shipmentService.getOverdueShipments();
        return ResponseEntity.ok(shipments);
    }

    /**
     * Update shipment status (Admin only)
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Shipment> updateShipmentStatus(
            @PathVariable Long id,
            @RequestParam ShipmentStatus status,
            @RequestParam String description,
            @RequestParam(required = false) String location) {

        try {
            Shipment updatedShipment = shipmentService.updateShipmentStatus(id, status, description, location);
            return ResponseEntity.ok(updatedShipment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update shipment (Admin only)
     */
    @PutMapping("/{id}")
    public ResponseEntity<Shipment> updateShipment(@PathVariable Long id, @RequestBody Shipment shipment) {
        try {
            shipment.setId(id);
            Shipment updatedShipment = shipmentService.updateShipment(shipment);
            return ResponseEntity.ok(updatedShipment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add tracking event (Admin only)
     */
    @PostMapping("/{id}/events")
    public ResponseEntity<TrackingEvent> addTrackingEvent(
            @PathVariable Long id,
            @RequestParam ShipmentStatus status,
            @RequestParam String description,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String operatorName) {

        try {
            TrackingEvent event = shipmentService.addTrackingEvent(
                    id, status, description, location, city, country, operatorName);
            return ResponseEntity.status(HttpStatus.CREATED).body(event);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get tracking events for shipment
     */
    @GetMapping("/{id}/events")
    public ResponseEntity<List<TrackingEvent>> getTrackingEvents(@PathVariable Long id) {
        try {
            List<TrackingEvent> events = shipmentService.getTrackingEvents(id);
            return ResponseEntity.ok(events);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get recent shipments for client (Client portal)
     */
    @GetMapping("/client/{clientId}/recent")
    public ResponseEntity<List<Shipment>> getRecentShipmentsByClient(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "5") int limit) {

        List<Shipment> shipments = shipmentService.getRecentShipmentsByClient(clientId, limit);
        return ResponseEntity.ok(shipments);
    }

    /**
     * Get shipment statistics (Admin only)
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getShipmentStatistics() {
        Map<String, Object> statistics = shipmentService.getShipmentStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Delete shipment (Admin only)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShipment(@PathVariable Long id) {
        try {
            shipmentService.deleteShipment(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all shipment statuses (for dropdowns)
     */
    @GetMapping("/statuses")
    public ResponseEntity<ShipmentStatus[]> getShipmentStatuses() {
        return ResponseEntity.ok(ShipmentStatus.values());
    }
}
