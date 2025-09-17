package com.freightops.fret.manifeste.controller;

import com.freightops.fret.manifeste.dto.ManifestCreateRequest;
import com.freightops.fret.manifeste.dto.ManifestResponse;
import com.freightops.fret.manifeste.service.ManifestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/fret/manifests")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ManifestController {

    private final ManifestService manifestService;

    /**
     * Créer un nouveau manifeste
     */
    @PostMapping
    public ResponseEntity<ManifestResponse> createManifest(@Valid @RequestBody ManifestCreateRequest request) {
        ManifestResponse response = manifestService.createManifest(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Récupérer tous les manifestes avec pagination
     */
    @GetMapping
    public ResponseEntity<Page<ManifestResponse>> getManifests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate,desc") String sort) {

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        Page<ManifestResponse> manifests = manifestService.getManifests(pageable);

        return ResponseEntity.ok(manifests);
    }

    /**
     * Récupérer un manifeste par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ManifestResponse> getManifestById(@PathVariable Long id) {
        ManifestResponse response = manifestService.getManifestById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer un manifeste par numéro
     */
    @GetMapping("/number/{manifestNumber}")
    public ResponseEntity<ManifestResponse> getManifestByNumber(@PathVariable String manifestNumber) {
        ManifestResponse response = manifestService.getManifestByNumber(manifestNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Mettre à jour un manifeste
     */
    @PutMapping("/{id}")
    public ResponseEntity<ManifestResponse> updateManifest(
            @PathVariable Long id,
            @Valid @RequestBody ManifestCreateRequest request) {
        ManifestResponse response = manifestService.updateManifest(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Mettre à jour le statut d'un manifeste
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ManifestResponse> updateManifestStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        String status = statusUpdate.get("status");
        ManifestResponse response = manifestService.updateManifestStatus(id, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprimer un manifeste
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteManifest(@PathVariable Long id) {
        manifestService.deleteManifest(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Rechercher des manifestes
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ManifestResponse>> searchManifests(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<ManifestResponse> results = manifestService.searchManifests(query, pageable);

        return ResponseEntity.ok(results);
    }

    /**
     * Récupérer les manifestes par statut
     */
    @GetMapping("/status")
    public ResponseEntity<Page<ManifestResponse>> getManifestsByStatus(
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<ManifestResponse> manifests = manifestService.getManifestsByStatus(status, pageable);

        return ResponseEntity.ok(manifests);
    }

    /**
     * Générer le PDF d'un manifeste
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generateManifestPdf(@PathVariable Long id) {
        System.out.println("=== ManifestController.generateManifestPdf called with ID: " + id + " ===");
        try {
            System.out.println("Calling manifestService.generateManifestPdf...");
            byte[] pdfBytes = manifestService.generateManifestPdf(id);
            System.out.println("PDF bytes received, length: " + (pdfBytes != null ? pdfBytes.length : 0));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "manifeste_" + id + ".pdf");
            headers.setContentLength(pdfBytes.length);

            System.out.println("Returning PDF response with headers");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("ERROR in ManifestController.generateManifestPdf: " + e.getClass().getSimpleName()
                    + " - " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Générer le document Word d'un manifeste
     */
    @GetMapping("/{id}/word")
    public ResponseEntity<byte[]> generateManifestWord(@PathVariable Long id) {
        byte[] wordBytes = manifestService.generateManifestWord(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/rtf"));
        headers.setContentDispositionFormData("attachment", "manifeste_" + id + ".rtf");
        headers.setContentLength(wordBytes.length);

        return new ResponseEntity<>(wordBytes, headers, HttpStatus.OK);
    }

    /**
     * Ajouter une signature de chargement
     */
    @PostMapping("/{id}/loading-signature")
    public ResponseEntity<ManifestResponse> addLoadingSignature(
            @PathVariable Long id,
            @RequestBody Map<String, String> signatureData) {

        String signature = signatureData.get("signature");
        String signatory = signatureData.get("signatory");
        String remarks = signatureData.get("remarks");

        ManifestResponse response = manifestService.addLoadingSignature(id, signature, signatory, remarks);
        return ResponseEntity.ok(response);
    }

    /**
     * Ajouter une signature de livraison
     */
    @PostMapping("/{id}/delivery-signature")
    public ResponseEntity<ManifestResponse> addDeliverySignature(
            @PathVariable Long id,
            @RequestBody Map<String, String> signatureData) {

        String signature = signatureData.get("signature");
        String signatory = signatureData.get("signatory");
        String remarks = signatureData.get("remarks");

        ManifestResponse response = manifestService.addDeliverySignature(id, signature, signatory, remarks);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer les statistiques des manifestes
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getManifestStats() {
        Map<String, Object> stats = manifestService.getManifestStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Valider un numéro de suivi
     */
    @GetMapping("/validate-tracking/{trackingNumber}")
    public ResponseEntity<Map<String, Boolean>> validateTrackingNumber(@PathVariable String trackingNumber) {
        Map<String, Boolean> validation = manifestService.validateTrackingNumber(trackingNumber);
        return ResponseEntity.ok(validation);
    }

    /**
     * Générer un QR code pour un manifeste
     */
    @PostMapping("/{id}/qr-code")
    public ResponseEntity<Map<String, String>> generateQRCode(@PathVariable Long id) {
        String qrCode = manifestService.generateQRCode(id);
        return ResponseEntity.ok(Map.of("qrCode", qrCode));
    }

    /**
     * Exporter les manifestes vers Excel
     */
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String transportMode,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo) {

        byte[] excelBytes = manifestService.exportToExcel(status, transportMode, dateFrom, dateTo);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "manifestes_export.xlsx");
        headers.setContentLength(excelBytes.length);

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    /**
     * Dupliquer un manifeste
     */
    @PostMapping("/{id}/duplicate")
    public ResponseEntity<ManifestResponse> duplicateManifest(@PathVariable Long id) {
        ManifestResponse response = manifestService.duplicateManifest(id);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Récupérer les manifestes récents
     */
    @GetMapping("/recent")
    public ResponseEntity<Page<ManifestResponse>> getRecentManifests(
            @RequestParam(defaultValue = "5") int limit) {

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<ManifestResponse> recentManifests = manifestService.getManifests(pageable);

        return ResponseEntity.ok(recentManifests);
    }

    /**
     * Récupérer les manifestes par période
     */
    @GetMapping("/period")
    public ResponseEntity<Page<ManifestResponse>> getManifestsByPeriod(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<ManifestResponse> manifests = manifestService.getManifestsByPeriod(startDate, endDate, pageable);

        return ResponseEntity.ok(manifests);
    }
}
