package com.freightops.fret.manifeste.service;

import com.freightops.fret.manifeste.model.FreightManifest;
import com.freightops.fret.manifeste.model.ManifestItem;
import com.freightops.fret.manifeste.dto.ManifestCreateRequest;
import com.freightops.fret.manifeste.dto.ManifestResponse;
import com.freightops.fret.manifeste.repository.FreightManifestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ManifestService {

    @Autowired
    private FreightManifestRepository freightManifestRepository;

    @Autowired
    private ManifestPdfService manifestPdfService;

    /**
     * Create a new manifest
     */
    public ManifestResponse createManifest(ManifestCreateRequest request) {
        FreightManifest freightManifest = new FreightManifest();

        // Basic information
        freightManifest.setProformaNumber(request.getProformaNumber());
        freightManifest
                .setTransportMode(request.getTransportMode() != null ? request.getTransportMode().toString() : null);
        freightManifest.setVehicleReference(request.getVehicleInfo());
        freightManifest.setDriverName(request.getDriverName());
        freightManifest.setScheduledDeparture(request.getDepartureDate());
        freightManifest.setScheduledArrival(request.getArrivalDate());
        freightManifest.setDeliveryInstructions(request.getDeliveryInstructions());
        freightManifest.setGeneralRemarks(request.getRemarks());
        freightManifest.setAttachments(request.getAttachments());
        freightManifest.setStatus("DRAFT");

        // Generate manifest number
        freightManifest.setManifestNumber(generateManifestNumber());

        // Save manifest
        freightManifest = freightManifestRepository.save(freightManifest);
        return convertToResponse(freightManifest);
    }

    /**
     * Get all manifests with pagination
     */
    @Transactional(readOnly = true)
    public Page<ManifestResponse> getManifests(Pageable pageable) {
        List<FreightManifest> freightManifests = freightManifestRepository.findAllOrderByCreatedAtDesc();
        List<ManifestResponse> responses = freightManifests.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        List<ManifestResponse> pageContent = responses.subList(start, end);

        return new PageImpl<>(pageContent, pageable, responses.size());
    }

    /**
     * Get manifest by ID
     */
    @Transactional(readOnly = true)
    public ManifestResponse getManifestById(Long id) {
        FreightManifest freightManifest = freightManifestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manifest not found with id: " + id));
        return convertToResponse(freightManifest);
    }

    /**
     * Get manifest by manifest number
     */
    @Transactional(readOnly = true)
    public ManifestResponse getManifestByNumber(String manifestNumber) {
        FreightManifest freightManifest = freightManifestRepository.findByManifestNumber(manifestNumber)
                .orElseThrow(() -> new RuntimeException("Manifest not found with number: " + manifestNumber));
        return convertToResponse(freightManifest);
    }

    /**
     * Update manifest
     */
    public ManifestResponse updateManifest(Long id, ManifestCreateRequest request) {
        FreightManifest freightManifest = freightManifestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manifest not found with id: " + id));

        // Update basic information
        freightManifest.setProformaNumber(request.getProformaNumber());
        freightManifest
                .setTransportMode(request.getTransportMode() != null ? request.getTransportMode().toString() : null);
        freightManifest.setVehicleReference(request.getVehicleInfo());
        freightManifest.setDriverName(request.getDriverName());
        freightManifest.setScheduledDeparture(request.getDepartureDate());
        freightManifest.setScheduledArrival(request.getArrivalDate());
        freightManifest.setDeliveryInstructions(request.getDeliveryInstructions());
        freightManifest.setGeneralRemarks(request.getRemarks());
        freightManifest.setAttachments(request.getAttachments());

        freightManifest = freightManifestRepository.save(freightManifest);
        return convertToResponse(freightManifest);
    }

    /**
     * Update manifest status - accepts String
     */
    public ManifestResponse updateManifestStatus(Long id, String status) {
        FreightManifest freightManifest = freightManifestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manifest not found with id: " + id));

        freightManifest.setStatus(status.toUpperCase());
        freightManifest = freightManifestRepository.save(freightManifest);
        return convertToResponse(freightManifest);
    }

    /**
     * Delete manifest
     */
    public void deleteManifest(Long id) {
        if (!freightManifestRepository.existsById(id)) {
            throw new RuntimeException("Manifest not found with id: " + id);
        }
        freightManifestRepository.deleteById(id);
    }

    /**
     * Search manifests
     */
    @Transactional(readOnly = true)
    public Page<ManifestResponse> searchManifests(String searchTerm, Pageable pageable) {
        List<FreightManifest> freightManifests = freightManifestRepository.findByShipperOrConsignee(searchTerm,
                searchTerm);
        List<ManifestResponse> responses = freightManifests.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        List<ManifestResponse> pageContent = responses.subList(start, end);

        return new PageImpl<>(pageContent, pageable, responses.size());
    }

    /**
     * Get manifests by status - accepts String parameter
     */
    @Transactional(readOnly = true)
    public Page<ManifestResponse> getManifestsByStatus(String status, Pageable pageable) {
        List<FreightManifest> freightManifests = freightManifestRepository.findByStatus(status.toUpperCase());
        List<ManifestResponse> responses = freightManifests.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        List<ManifestResponse> pageContent = responses.subList(start, end);

        return new PageImpl<>(pageContent, pageable, responses.size());
    }

    /**
     * Get manifests by period
     */
    @Transactional(readOnly = true)
    public Page<ManifestResponse> getManifestsByPeriod(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        List<FreightManifest> freightManifests = freightManifestRepository.findByCreatedAtBetween(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59));
        List<ManifestResponse> responses = freightManifests.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        List<ManifestResponse> pageContent = responses.subList(start, end);

        return new PageImpl<>(pageContent, pageable, responses.size());
    }

    /**
     * Add loading signature - corrected signature with 4 parameters
     */
    public ManifestResponse addLoadingSignature(Long id, String signature, String signatory, String remarks) {
        FreightManifest freightManifest = freightManifestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manifest not found with id: " + id));

        freightManifest.setLoadingSignature(signature);
        freightManifest.setLoadingSignatory(signatory);
        freightManifest.setLoadingSignatureDate(LocalDateTime.now());

        freightManifest = freightManifestRepository.save(freightManifest);
        return convertToResponse(freightManifest);
    }

    /**
     * Add delivery signature - corrected signature with 4 parameters
     */
    public ManifestResponse addDeliverySignature(Long id, String signature, String signatory, String remarks) {
        FreightManifest freightManifest = freightManifestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manifest not found with id: " + id));

        freightManifest.setDeliverySignature(signature);
        freightManifest.setDeliverySignatory(signatory);
        freightManifest.setDeliverySignatureDate(LocalDateTime.now());
        freightManifest.setDeliveryRemarks(remarks);

        // Automatically update status to DELIVERED when delivery signature is added
        if (!"DELIVERED".equals(freightManifest.getStatus())) {
            freightManifest.setStatus("DELIVERED");
        }

        freightManifest = freightManifestRepository.save(freightManifest);
        return convertToResponse(freightManifest);
    }

    /**
     * Generate manifest PDF
     */
    public byte[] generateManifestPdf(Long id) {
        System.out.println("=== ManifestService.generateManifestPdf called with ID: " + id + " ===");
        try {
            // Vérifier que le manifeste existe
            FreightManifest manifest = freightManifestRepository.findById(id).orElse(null);
            if (manifest == null) {
                System.err.println("ERROR: Manifest not found with ID: " + id);
                throw new RuntimeException("Manifest not found with id: " + id);
            }
            System.out.println("Manifest found: " + manifest.getManifestNumber());

            // Vérifier que le service PDF est injecté
            if (manifestPdfService == null) {
                System.err.println("ERROR: ManifestPdfService is null - dependency injection failed");
                throw new RuntimeException("ManifestPdfService not available");
            }
            System.out.println("ManifestPdfService is available, calling generateManifestPdf...");

            byte[] result = manifestPdfService.generateManifestPdf(id);
            System.out.println("PDF generated successfully, size: " + (result != null ? result.length : 0) + " bytes");
            return result;
        } catch (Exception e) {
            System.err.println("ERROR in ManifestService.generateManifestPdf: " + e.getClass().getSimpleName() + " - "
                    + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate manifest PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Generate manifest Word document
     */
    public byte[] generateManifestWord(Long id) {
        try {
            return manifestPdfService.generateManifestWord(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate manifest Word document: " + e.getMessage(), e);
        }
    }

    /**
     * Validate tracking number
     */
    @Transactional(readOnly = true)
    public Map<String, Boolean> validateTrackingNumber(String trackingNumber) {
        // Since we don't have findByTrackingNumber in repository, we'll check items
        boolean exists = false; // TODO: Implement proper tracking number validation
        Map<String, Boolean> result = new HashMap<>();
        result.put("valid", !exists);
        result.put("exists", exists);
        return result;
    }

    /**
     * Generate QR code for manifest
     */
    public String generateQRCode(Long id) {
        FreightManifest freightManifest = freightManifestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manifest not found with id: " + id));

        // Generate QR code data (URL for tracking)
        String qrData = "https://freightops.com/track/" + freightManifest.getManifestNumber();

        // Update manifest with QR code
        freightManifest.setQrCode(qrData);
        freightManifestRepository.save(freightManifest);

        return qrData;
    }

    /**
     * Export manifests to Excel
     */
    public byte[] exportToExcel(String status, String transportMode, LocalDate dateFrom, LocalDate dateTo) {
        // TODO: Implement Excel export
        // For now, return placeholder
        return "Excel content placeholder".getBytes();
    }

    /**
     * Duplicate manifest
     */
    public ManifestResponse duplicateManifest(Long id) {
        FreightManifest original = freightManifestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manifest not found with id: " + id));

        FreightManifest duplicate = new FreightManifest();

        // Copy basic information
        duplicate.setProformaNumber(original.getProformaNumber() + "_COPY");
        duplicate.setTransportMode(original.getTransportMode());
        duplicate.setVehicleReference(original.getVehicleReference());
        duplicate.setDriverName(original.getDriverName());
        duplicate.setDriverPhone(original.getDriverPhone());
        duplicate.setScheduledDeparture(original.getScheduledDeparture());
        duplicate.setScheduledArrival(original.getScheduledArrival());
        duplicate.setDeliveryInstructions(original.getDeliveryInstructions());
        duplicate.setGeneralRemarks(original.getGeneralRemarks());
        duplicate.setAttachments(original.getAttachments());
        duplicate.setStatus("DRAFT");

        // Copy party information
        duplicate.setShipperName(original.getShipperName());
        duplicate.setShipperAddress(original.getShipperAddress());
        duplicate.setShipperContact(original.getShipperContact());
        duplicate.setShipperPhone(original.getShipperPhone());

        duplicate.setConsigneeName(original.getConsigneeName());
        duplicate.setConsigneeAddress(original.getConsigneeAddress());
        duplicate.setConsigneeContact(original.getConsigneeContact());
        duplicate.setConsigneePhone(original.getConsigneePhone());

        duplicate.setClientName(original.getClientName());
        duplicate.setClientReference(original.getClientReference());
        duplicate.setClientContact(original.getClientContact());
        duplicate.setClientPhone(original.getClientPhone());

        duplicate.setAgentName(original.getAgentName());
        duplicate.setAgentAddress(original.getAgentAddress());
        duplicate.setAgentContact(original.getAgentContact());
        duplicate.setAgentPhone(original.getAgentPhone());

        // Generate new manifest number
        duplicate.setManifestNumber(generateManifestNumber());

        duplicate = freightManifestRepository.save(duplicate);
        return convertToResponse(duplicate);
    }

    /**
     * Get manifest statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getManifestStats() {
        Map<String, Object> stats = new HashMap<>();

        List<FreightManifest> allManifests = freightManifestRepository.findAll();
        stats.put("totalCount", (long) allManifests.size());
        stats.put("draftCount", freightManifestRepository.countByStatus("DRAFT"));
        stats.put("confirmedCount", freightManifestRepository.countByStatus("CONFIRMED"));
        stats.put("inTransitCount", freightManifestRepository.countByStatus("IN_TRANSIT"));
        stats.put("deliveredCount", freightManifestRepository.countByStatus("DELIVERED"));
        stats.put("cancelledCount", freightManifestRepository.countByStatus("CANCELLED"));

        // Calculate averages
        BigDecimal avgWeight = allManifests.stream()
                .filter(m -> m.getTotalWeight() != null)
                .map(FreightManifest::getTotalWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(1, allManifests.size())), 2, BigDecimal.ROUND_HALF_UP);
        stats.put("averageWeight", avgWeight);

        BigDecimal avgValue = allManifests.stream()
                .filter(m -> m.getTotalValue() != null)
                .map(FreightManifest::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(1, allManifests.size())), 2, BigDecimal.ROUND_HALF_UP);
        stats.put("averageValue", avgValue);

        return stats;
    }

    /**
     * Convert FreightManifest entity to ManifestResponse DTO
     */
    private ManifestResponse convertToResponse(FreightManifest freightManifest) {
        ManifestResponse response = new ManifestResponse();

        // Basic information
        response.setId(freightManifest.getId());
        response.setManifestNumber(freightManifest.getManifestNumber());
        response.setProformaNumber(freightManifest.getProformaNumber());
        response.setTransportMode(freightManifest.getTransportMode());
        response.setVehicleReference(freightManifest.getVehicleReference());
        response.setDriverName(freightManifest.getDriverName());
        response.setDriverPhone(freightManifest.getDriverPhone());
        response.setScheduledDeparture(freightManifest.getScheduledDeparture());
        response.setScheduledArrival(freightManifest.getScheduledArrival());
        response.setDeliveryInstructions(freightManifest.getDeliveryInstructions());
        response.setGeneralRemarks(freightManifest.getGeneralRemarks());
        response.setAttachments(freightManifest.getAttachments());
        response.setStatus(freightManifest.getStatus());

        // Party information
        response.setShipperName(freightManifest.getShipperName());
        response.setShipperAddress(freightManifest.getShipperAddress());
        response.setShipperContact(freightManifest.getShipperContact());
        response.setShipperPhone(freightManifest.getShipperPhone());

        response.setConsigneeName(freightManifest.getConsigneeName());
        response.setConsigneeAddress(freightManifest.getConsigneeAddress());
        response.setConsigneeContact(freightManifest.getConsigneeContact());
        response.setConsigneePhone(freightManifest.getConsigneePhone());

        response.setClientName(freightManifest.getClientName());
        response.setClientReference(freightManifest.getClientReference());
        response.setClientContact(freightManifest.getClientContact());
        response.setClientPhone(freightManifest.getClientPhone());

        response.setAgentName(freightManifest.getAgentName());
        response.setAgentAddress(freightManifest.getAgentAddress());
        response.setAgentContact(freightManifest.getAgentContact());
        response.setAgentPhone(freightManifest.getAgentPhone());

        // Totals
        response.setTotalWeight(freightManifest.getTotalWeight());
        response.setTotalVolume(freightManifest.getTotalVolume());
        response.setTotalVolumetricWeight(freightManifest.getTotalVolumetricWeight());
        response.setTotalValue(freightManifest.getTotalValue());
        response.setTotalPackages(freightManifest.getTotalPackages());

        // Signatures
        response.setLoadingSignature(freightManifest.getLoadingSignature());
        response.setLoadingSignatory(freightManifest.getLoadingSignatory());
        response.setLoadingSignatureDate(freightManifest.getLoadingSignatureDate());

        response.setDeliverySignature(freightManifest.getDeliverySignature());
        response.setDeliverySignatory(freightManifest.getDeliverySignatory());
        response.setDeliverySignatureDate(freightManifest.getDeliverySignatureDate());
        response.setDeliveryRemarks(freightManifest.getDeliveryRemarks());

        // Convert items to ManifestItemResponse
        if (freightManifest.getItems() != null && !freightManifest.getItems().isEmpty()) {
            List<ManifestResponse.ManifestItemResponse> items = freightManifest.getItems().stream()
                    .map(item -> {
                        ManifestResponse.ManifestItemResponse itemResponse = new ManifestResponse.ManifestItemResponse();
                        itemResponse.setId(item.getId());
                        itemResponse.setLineNumber(item.getLineNumber());
                        itemResponse.setTrackingNumber(item.getTrackingNumber());
                        itemResponse.setDescription(item.getDescription());
                        itemResponse.setPackagingType(item.getPackagingType());
                        itemResponse.setPackageCount(item.getPackageCount());
                        itemResponse.setGrossWeight(item.getGrossWeight());
                        itemResponse.setVolume(item.getVolume());
                        itemResponse.setVolumetricWeight(item.getVolumetricWeight());
                        itemResponse.setDeclaredValue(item.getDeclaredValue());
                        itemResponse.setContainerNumber(item.getContainerNumber());
                        itemResponse.setRemarks(item.getRemarks());
                        return itemResponse;
                    })
                    .collect(Collectors.toList());
            response.setItems(items);
        }

        // Metadata
        response.setQrCode(freightManifest.getQrCode());
        response.setCreatedAt(freightManifest.getCreatedAt());
        response.setUpdatedAt(freightManifest.getUpdatedAt());

        return response;
    }

    /**
     * Generate unique manifest number
     */
    private String generateManifestNumber() {
        return "MAN-" + System.currentTimeMillis();
    }
}
