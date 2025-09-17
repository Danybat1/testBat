package com.freightops.api;

import com.freightops.dto.LTADto;
import com.freightops.dto.LTAResponse;
import com.freightops.dto.LTAStatusHistoryResponse;
import com.freightops.dto.CityResponse;
import com.freightops.dto.ClientResponse;
import com.freightops.dto.PackageResponse;
import com.freightops.entity.LTA;
import com.freightops.entity.LTAStatusHistory;
import com.freightops.enums.LTAStatus;
import com.freightops.service.LTAService;
import com.freightops.service.LTAJasperService;
import com.freightops.dto.ApiResponse;
import com.freightops.dto.LTARequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * LTA REST Controller
 * Handles HTTP requests for LTA operations
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LTAController {

    private final LTAService ltaService;
    private final LTAJasperService ltaJasperService;
    private static final Logger logger = LoggerFactory.getLogger(LTAController.class);

    public LTAController(LTAService ltaService, LTAJasperService ltaJasperService) {
        this.ltaService = ltaService;
        this.ltaJasperService = ltaJasperService;
    }

    /**
     * Create a new LTA
     * POST /api/lta
     */
    @PostMapping("/lta")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')") // Disabled for
    // development
    public ResponseEntity<?> createLTA(@Valid @RequestBody LTARequest ltaRequest) {
        logger.info("🚀 === DÉBUT CRÉATION LTA BACKEND ===");
        logger.info("📥 Requête reçue: {}", ltaRequest);

        try {
            // Log des données reçues
            logger.info("🏙️ Ville origine ID: {}", ltaRequest.getOriginCityId());
            logger.info("🏙️ Ville destination ID: {}", ltaRequest.getDestinationCityId());
            logger.info("💰 Mode paiement: {}", ltaRequest.getPaymentMode());
            logger.info("👤 Client ID: {}", ltaRequest.getClientId());
            logger.info("📦 Poids total: {}", ltaRequest.getTotalWeight());
            logger.info("📦 Nature colis: {}", ltaRequest.getPackageNature());
            logger.info("📦 Nombre colis: {}", ltaRequest.getPackageCount());

            // Validate business rules
            if (!ltaRequest.isValid()) {
                logger.warn("❌ Échec validation règles métier: Client requis pour mode TO_INVOICE");
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Client is required when payment mode is TO_INVOICE"));
            }

            logger.info("✅ Validation règles métier réussie");
            logger.info("🔄 Appel service création LTA...");

            LTA createdLTA = ltaService.createLTA(ltaRequest);

            logger.info("✅ LTA créée avec succès:");
            logger.info("✅ ID: {}", createdLTA.getId());
            logger.info("✅ Numéro LTA: {}", createdLTA.getLtaNumber());
            logger.info("✅ Tracking Number: {}", createdLTA.getTrackingNumber());
            logger.info("✅ Statut: {}", createdLTA.getStatus());

            LTAResponse response = convertToResponse(createdLTA);
            logger.info("🏁 === FIN CRÉATION LTA BACKEND ===");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Erreur validation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ Erreur interne lors de la création LTA:", e);
            logger.error("❌ Message d'erreur: {}", e.getMessage());
            logger.error("❌ Cause: {}", e.getCause());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to create LTA: " + e.getMessage()));
        }
    }

    /**
     * Get all LTAs with pagination and filtering
     * GET /api/lta
     */
    @GetMapping("/lta")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('FINANCE')")
    // // Disabled for development
    public ResponseEntity<Page<LTAResponse>> getAllLTAs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) LTAStatus status,
            @RequestParam(required = false) String shipper,
            @RequestParam(required = false) String consignee) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<LTA> ltaPage;
        if (status != null || shipper != null || consignee != null) {
            ltaPage = ltaService.searchLTAs(status, shipper, consignee, pageable);
        } else {
            ltaPage = ltaService.getAllLTAs(pageable);
        }

        Page<LTAResponse> ltaResponsePage = ltaPage.map(this::convertToResponse);
        return ResponseEntity.ok(ltaResponsePage);
    }

    /**
     * Get LTA by ID
     * GET /api/lta/{id}
     */
    @GetMapping("/lta/{id}")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('FINANCE')")
    // // Disabled for development
    public ResponseEntity<?> getLTAById(@PathVariable Long id) {
        logger.info("🔍 === GET LTA BY ID ===");
        logger.info("🔍 ID demandé: {}", id);

        try {
            Optional<LTA> ltaOpt = ltaService.getLTAById(id);
            if (ltaOpt.isPresent()) {
                LTA lta = ltaOpt.get();
                logger.info("✅ LTA trouvée - ID: {}, Numéro: {}, Tracking: {}",
                        lta.getId(), lta.getLtaNumber(), lta.getTrackingNumber());
                logger.info("✅ Statut: {}, Client: {}", lta.getStatus(),
                        lta.getClient() != null ? lta.getClient().getName() : "null");

                LTAResponse response = convertToResponse(lta);
                logger.info("✅ Réponse convertie avec trackingNumber: {}", response.getTrackingNumber());

                return ResponseEntity.ok(ApiResponse.success(response));
            } else {
                logger.warn("❌ LTA non trouvée avec l'ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("LTA non trouvée avec l'ID: " + id));
            }
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la recherche de LTA par ID: {} - {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur interne du serveur"));
        }
    }

    /**
     * Get LTA by LTA number
     * GET /api/lta/number/{ltaNumber}
     */
    @GetMapping("/lta/number/{ltaNumber}")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('FINANCE')")
    // // Disabled for development
    public ResponseEntity<?> getLTAByNumber(@PathVariable String ltaNumber) {
        Optional<LTA> ltaOpt = ltaService.getLTAByNumber(ltaNumber);
        if (ltaOpt.isPresent()) {
            return ResponseEntity.ok(convertToResponse(ltaOpt.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get LTA by tracking number
     * GET /api/lta/tracking/{trackingNumber}
     */
    @GetMapping("/lta/tracking/{trackingNumber}")
    public ResponseEntity<?> getLTAByTrackingNumber(@PathVariable String trackingNumber) {
        try {
            Optional<LTA> ltaOpt = ltaService.getLTAByTrackingNumber(trackingNumber);
            if (ltaOpt.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(convertToResponse(ltaOpt.get())));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("LTA non trouvée avec le numéro de suivi: " + trackingNumber));
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de LTA par numéro de suivi: {}", trackingNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur interne du serveur"));
        }
    }

    /**
     * Get LTA by tracking number (PUBLIC ENDPOINT)
     * GET /api/public/tracking/{trackingNumber}
     */
    @GetMapping("/public/tracking/{trackingNumber}")
    public ResponseEntity<?> getPublicLTAByTrackingNumber(@PathVariable String trackingNumber) {
        logger.info("🔍 === PUBLIC TRACKING REQUEST ===");
        logger.info("🔍 Tracking number demandé: {}", trackingNumber);

        try {
            Optional<LTA> ltaOpt = ltaService.getLTAByTrackingNumber(trackingNumber);
            if (ltaOpt.isPresent()) {
                LTA lta = ltaOpt.get();
                logger.info("✅ LTA trouvée pour tracking: {} - ID: {}, Numéro: {}",
                        trackingNumber, lta.getId(), lta.getLtaNumber());

                // Créer une réponse publique limitée (sans informations sensibles)
                LTAResponse response = convertToPublicResponse(lta);
                logger.info("✅ Réponse publique créée pour tracking: {}", trackingNumber);

                return ResponseEntity.ok(ApiResponse.success(response));
            } else {
                logger.warn("❌ Aucune LTA trouvée pour le tracking number: {}", trackingNumber);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Aucun envoi trouvé avec ce numéro de suivi: " + trackingNumber));
            }
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la recherche publique par tracking: {} - {}", trackingNumber, e.getMessage(),
                    e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la recherche"));
        }
    }

    /**
     * Update LTA
     * PUT /api/lta/{id}
     */
    @PutMapping("/lta/{id}")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')") // Disabled for
    // development
    public ResponseEntity<?> updateLTA(@PathVariable Long id, @Valid @RequestBody LTADto ltaDto) {
        try {
            Optional<LTA> updatedLTA = ltaService.updateLTA(id, ltaDto);
            if (updatedLTA.isPresent()) {
                return ResponseEntity.ok(convertToResponse(updatedLTA.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to update LTA: " + e.getMessage()));
        }
    }

    /**
     * Update LTA status
     * PATCH /api/lta/{id}/status
     */
    @PatchMapping("/lta/{id}/status")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')") // Disabled for
    // development
    public ResponseEntity<?> updateLTAStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        try {
            Optional<LTA> updatedLTA = ltaService.updateLTAStatus(id, request.getStatus());
            if (updatedLTA.isPresent()) {
                return ResponseEntity.ok(convertToResponse(updatedLTA.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to update LTA status: " + e.getMessage()));
        }
    }

    /**
     * Delete LTA
     * DELETE /api/lta/{id}
     */
    @DeleteMapping("/lta/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // Disabled for development
    public ResponseEntity<?> deleteLTA(@PathVariable Long id) {
        try {
            boolean deleted = ltaService.deleteLTA(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to delete LTA: " + e.getMessage()));
        }
    }

    /**
     * Get LTA statistics
     * GET /api/lta/stats
     */
    @GetMapping("/lta/stats")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('FINANCE')")
    // // Disabled for development
    public ResponseEntity<LTAStatsResponse> getLTAStats() {
        LTAStatsResponse stats = new LTAStatsResponse();
        stats.setPendingCount(ltaService.getCountByStatus(LTAStatus.DRAFT));
        stats.setConfirmedCount(ltaService.getCountByStatus(LTAStatus.CONFIRMED));
        stats.setInTransitCount(ltaService.getCountByStatus(LTAStatus.IN_TRANSIT));
        stats.setDeliveredCount(ltaService.getCountByStatus(LTAStatus.DELIVERED));
        stats.setCancelledCount(ltaService.getCountByStatus(LTAStatus.CANCELLED));

        return ResponseEntity.ok(stats);
    }

    /**
     * Get recent LTAs
     * GET /api/lta/recent
     */
    @GetMapping("/lta/recent")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('FINANCE')")
    // // Disabled for development
    public ResponseEntity<Page<LTAResponse>> getRecentLTAs(@RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        Page<LTA> recentLTAs = ltaService.getAllLTAs(pageable);
        Page<LTAResponse> ltaResponsePage = recentLTAs.map(this::convertToResponse);
        return ResponseEntity.ok(ltaResponsePage);
    }

    /**
     * Calculate cost for LTA based on origin, destination and weight
     * GET /api/lta/calculate-cost
     */
    @GetMapping("/lta/calculate-cost")
    public ResponseEntity<?> calculateCost(
            @RequestParam Long originCityId,
            @RequestParam Long destinationCityId,
            @RequestParam Double weight) {
        try {
            Double cost = ltaService.calculateCost(originCityId, destinationCityId, weight);
            CostCalculationResponse response = new CostCalculationResponse(cost);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to calculate cost: " + e.getMessage()));
        }
    }

    /**
     * Generate PDF for LTA
     * GET /api/lta/{id}/pdf
     */
    @GetMapping("/lta/{id}/pdf")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('FINANCE')")
    // // Disabled for development
    public ResponseEntity<?> generateLTAPdf(@PathVariable Long id) {
        Optional<LTA> ltaOptional = ltaService.getLTAById(id);
        if (ltaOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        LTA lta = ltaOptional.get();

        try {
            byte[] pdfBytes = ltaJasperService.generateLTAPdf(lta);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "LTA-" + lta.getLtaNumber() + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating PDF: " + e.getMessage());
        }
    }

    private LTAResponse convertToResponse(LTA lta) {
        LTAResponse response = new LTAResponse();
        response.setId(lta.getId());
        response.setLtaNumber(lta.getLtaNumber());
        response.setTrackingNumber(lta.getTrackingNumber()); // Ajout du mapping trackingNumber
        response.setStatus(lta.getStatus());
        response.setShipperName(lta.getShipperName());
        response.setShipperAddress(lta.getShipperAddress());
        response.setConsigneeName(lta.getConsigneeName());
        response.setConsigneeAddress(lta.getConsigneeAddress());
        response.setTotalWeight(lta.getTotalWeight());
        response.setPackageNature(lta.getPackageNature());
        response.setPackageCount(lta.getPackageCount());
        response.setCalculatedCost(lta.getCalculatedCost());
        response.setPaymentMode(lta.getPaymentMode());
        response.setSpecialInstructions(lta.getSpecialInstructions());
        response.setDeclaredValue(lta.getDeclaredValue());
        response.setPickupDate(lta.getPickupDate());
        response.setDeliveryDate(lta.getDeliveryDate());
        response.setCreatedAt(lta.getCreatedAt());
        response.setUpdatedAt(lta.getUpdatedAt());

        // Set city information
        if (lta.getOriginCity() != null) {
            CityResponse originCity = new CityResponse();
            originCity.setId(lta.getOriginCity().getId());
            originCity.setName(lta.getOriginCity().getName());
            originCity.setIataCode(lta.getOriginCity().getIataCode());
            response.setOriginCity(originCity);
        }

        if (lta.getDestinationCity() != null) {
            CityResponse destinationCity = new CityResponse();
            destinationCity.setId(lta.getDestinationCity().getId());
            destinationCity.setName(lta.getDestinationCity().getName());
            destinationCity.setIataCode(lta.getDestinationCity().getIataCode());
            response.setDestinationCity(destinationCity);
        }

        // Set client information
        if (lta.getClient() != null) {
            ClientResponse client = new ClientResponse();
            client.setId(lta.getClient().getId());
            client.setName(lta.getClient().getName());
            client.setAddress(lta.getClient().getAddress());
            client.setContactNumber(lta.getClient().getContactNumber());
            client.setEmail(lta.getClient().getEmail());
            response.setClient(client);
        }

        return response;
    }

    private LTAResponse convertToPublicResponse(LTA lta) {
        LTAResponse response = new LTAResponse();
        response.setId(lta.getId());
        response.setLtaNumber(lta.getLtaNumber());
        response.setTrackingNumber(lta.getTrackingNumber());
        response.setStatus(lta.getStatus());
        response.setShipperName(lta.getShipperName());
        response.setConsigneeName(lta.getConsigneeName());
        response.setTotalWeight(lta.getTotalWeight());
        response.setPackageNature(lta.getPackageNature());
        response.setPackageCount(lta.getPackageCount());
        response.setPickupDate(lta.getPickupDate());
        response.setDeliveryDate(lta.getDeliveryDate());
        response.setCreatedAt(lta.getCreatedAt());
        response.setUpdatedAt(lta.getUpdatedAt());

        // Set city information with full details
        if (lta.getOriginCity() != null) {
            CityResponse originCity = new CityResponse();
            originCity.setId(lta.getOriginCity().getId());
            originCity.setName(lta.getOriginCity().getName());
            originCity.setIataCode(lta.getOriginCity().getIataCode());
            originCity.setCountry(lta.getOriginCity().getCountry());
            response.setOriginCity(originCity);
        }

        if (lta.getDestinationCity() != null) {
            CityResponse destinationCity = new CityResponse();
            destinationCity.setId(lta.getDestinationCity().getId());
            destinationCity.setName(lta.getDestinationCity().getName());
            destinationCity.setIataCode(lta.getDestinationCity().getIataCode());
            destinationCity.setCountry(lta.getDestinationCity().getCountry());
            response.setDestinationCity(destinationCity);
        }

        // Add status history for public tracking
        try {
            List<LTAStatusHistory> statusHistory = ltaService.getStatusHistoryByTrackingNumber(lta.getTrackingNumber());
            List<LTAStatusHistoryResponse> historyResponses = statusHistory.stream()
                    .map(this::convertToHistoryResponse)
                    .collect(Collectors.toList());

            // Add current status if no history exists
            if (historyResponses.isEmpty()) {
                LTAStatusHistoryResponse currentStatus = new LTAStatusHistoryResponse(
                        null,
                        lta.getStatus().toString(),
                        "SYSTEM",
                        lta.getCreatedAt());
                historyResponses.add(currentStatus);
            }

            response.setStatusHistory(historyResponses);
            logger.info("✅ Added {} status history entries to public response", historyResponses.size());
        } catch (Exception e) {
            logger.error("❌ Error loading status history for tracking {}: {}", lta.getTrackingNumber(), e.getMessage());
        }

        return response;
    }

    private LTAStatusHistoryResponse convertToHistoryResponse(LTAStatusHistory history) {
        LTAStatusHistoryResponse response = new LTAStatusHistoryResponse();
        response.setId(history.getId());
        response.setPreviousStatus(history.getPreviousStatus());
        response.setNewStatus(history.getNewStatus());
        response.setChangedBy(history.getChangedBy());
        response.setChangeReason(history.getChangeReason());
        response.setChangedAt(history.getChangedAt());
        response.setStatusLabel(getStatusLabel(history.getNewStatus()));
        response.setStatusDescription(getStatusDescription(history.getNewStatus()));
        return response;
    }

    private String getStatusLabel(String status) {
        if (status == null)
            return "Inconnu";
        return switch (status) {
            case "DRAFT" -> "Brouillon";
            case "CONFIRMED" -> "Confirmé";
            case "IN_TRANSIT" -> "En transit";
            case "DELIVERED" -> "Livré";
            case "CANCELLED" -> "Annulé";
            default -> status;
        };
    }

    private String getStatusDescription(String status) {
        if (status == null)
            return "Statut inconnu";
        return switch (status) {
            case "DRAFT" -> "LTA créée en brouillon";
            case "CONFIRMED" -> "LTA confirmée et prête pour expédition";
            case "IN_TRANSIT" -> "Colis en cours de transport";
            case "DELIVERED" -> "Colis livré à destination";
            case "CANCELLED" -> "LTA annulée";
            default -> "Changement de statut vers " + status;
        };
    }

    /**
     * Convert LTA entity to DTO (for backward compatibility)
     */
    private LTADto convertToDto(LTA lta) {
        LTADto dto = new LTADto();
        dto.setId(lta.getId());
        dto.setLtaNumber(lta.getLtaNumber());
        dto.setStatus(lta.getStatus());
        dto.setShipper(lta.getShipperName());
        dto.setConsignee(lta.getConsigneeName());
        dto.setWeight(lta.getTotalWeight());
        dto.setTrackingNumber("TRK-" + lta.getId()); // Generate tracking number from ID
        dto.setDescription(lta.getPackageNature()); // Use package nature as description
        dto.setOrigin(lta.getOriginCity() != null ? lta.getOriginCity().getName() : null);
        dto.setDestination(lta.getDestinationCity() != null ? lta.getDestinationCity().getName() : null);
        dto.setDeclaredValue(lta.getDeclaredValue());
        dto.setCreatedAt(lta.getCreatedAt());
        dto.setUpdatedAt(lta.getUpdatedAt());
        dto.setVersion(1L); // Set default version
        return dto;
    }

    // Inner classes for request/response objects
    public static class StatusUpdateRequest {
        private LTAStatus status;

        public LTAStatus getStatus() {
            return status;
        }

        public void setStatus(LTAStatus status) {
            this.status = status;
        }
    }

    public static class ErrorResponse {
        private String message;
        private long timestamp;

        public ErrorResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static class LTAStatsResponse {
        private long pendingCount;
        private long confirmedCount;
        private long inTransitCount;
        private long deliveredCount;
        private long cancelledCount;

        // Getters and Setters
        public long getPendingCount() {
            return pendingCount;
        }

        public void setPendingCount(long pendingCount) {
            this.pendingCount = pendingCount;
        }

        public long getConfirmedCount() {
            return confirmedCount;
        }

        public void setConfirmedCount(long confirmedCount) {
            this.confirmedCount = confirmedCount;
        }

        public long getInTransitCount() {
            return inTransitCount;
        }

        public void setInTransitCount(long inTransitCount) {
            this.inTransitCount = inTransitCount;
        }

        public long getDeliveredCount() {
            return deliveredCount;
        }

        public void setDeliveredCount(long deliveredCount) {
            this.deliveredCount = deliveredCount;
        }

        public long getCancelledCount() {
            return cancelledCount;
        }

        public void setCancelledCount(long cancelledCount) {
            this.cancelledCount = cancelledCount;
        }
    }

    public static class CostCalculationResponse {
        private Double cost;

        public CostCalculationResponse(Double cost) {
            this.cost = cost;
        }

        public Double getCost() {
            return cost;
        }

        public void setCost(Double cost) {
            this.cost = cost;
        }
    }
}
