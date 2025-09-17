package com.freightops.api;

import com.freightops.entity.Tariff;
import com.freightops.dto.TariffRequest;
import com.freightops.dto.TariffResponse;
import com.freightops.dto.CityResponse;
import com.freightops.service.TariffService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Tariff REST Controller
 * Handles HTTP requests for Tariff operations
 */
@RestController
@RequestMapping("/api/tariff")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TariffController {

    private final TariffService tariffService;

    @Autowired
    public TariffController(TariffService tariffService) {
        this.tariffService = tariffService;
    }

    /**
     * Create a new tariff
     * POST /api/tariff
     */
    @PostMapping
    public ResponseEntity<?> createTariff(@Valid @RequestBody TariffRequest tariffRequest) {
        try {
            Tariff createdTariff = tariffService.createTariff(tariffRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponse(createdTariff));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to create tariff: " + e.getMessage()));
        }
    }

    /**
     * Get all tariffs with pagination
     * GET /api/tariff
     */
    @GetMapping
    public ResponseEntity<Page<TariffResponse>> getAllTariffs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Tariff> tariffPage = tariffService.getAllTariffs(pageable);
        Page<TariffResponse> tariffResponsePage = tariffPage.map(this::convertToResponse);
        return ResponseEntity.ok(tariffResponsePage);
    }

    /**
     * Get tariff by origin and destination
     * GET /api/tariff/route
     */
    @GetMapping("/route")
    public ResponseEntity<?> getTariffByRoute(
            @RequestParam Long originCityId,
            @RequestParam Long destinationCityId) {
        Optional<Tariff> tariffOpt = tariffService.getTariffByRoute(originCityId, destinationCityId);
        if (tariffOpt.isPresent()) {
            return ResponseEntity.ok(convertToResponse(tariffOpt.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get tariff by ID
     * GET /api/tariff/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTariffById(@PathVariable Long id) {
        Optional<Tariff> tariffOpt = tariffService.getTariffById(id);
        if (tariffOpt.isPresent()) {
            return ResponseEntity.ok(convertToResponse(tariffOpt.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update tariff
     * PUT /api/tariff/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTariff(@PathVariable Long id, @Valid @RequestBody TariffRequest tariffRequest) {
        try {
            Optional<Tariff> updatedTariff = tariffService.updateTariff(id, tariffRequest);
            if (updatedTariff.isPresent()) {
                return ResponseEntity.ok(convertToResponse(updatedTariff.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to update tariff: " + e.getMessage()));
        }
    }

    /**
     * Delete tariff
     * DELETE /api/tariff/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTariff(@PathVariable Long id) {
        try {
            boolean deleted = tariffService.deleteTariff(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to delete tariff: " + e.getMessage()));
        }
    }

    /**
     * Convert Tariff entity to Response DTO
     */
    private TariffResponse convertToResponse(Tariff tariff) {
        TariffResponse response = new TariffResponse();
        response.setId(tariff.getId());
        
        // Convert origin city to CityResponse
        CityResponse originCityResponse = new CityResponse();
        originCityResponse.setId(tariff.getOriginCity().getId());
        originCityResponse.setName(tariff.getOriginCity().getName());
        originCityResponse.setIataCode(tariff.getOriginCity().getIataCode());
        originCityResponse.setCountry(tariff.getOriginCity().getCountry());
        originCityResponse.setIsActive(tariff.getOriginCity().getIsActive());
        response.setOriginCity(originCityResponse);
        
        // Convert destination city to CityResponse
        CityResponse destinationCityResponse = new CityResponse();
        destinationCityResponse.setId(tariff.getDestinationCity().getId());
        destinationCityResponse.setName(tariff.getDestinationCity().getName());
        destinationCityResponse.setIataCode(tariff.getDestinationCity().getIataCode());
        destinationCityResponse.setCountry(tariff.getDestinationCity().getCountry());
        destinationCityResponse.setIsActive(tariff.getDestinationCity().getIsActive());
        response.setDestinationCity(destinationCityResponse);
        
        response.setKgRate(tariff.getKgRate());
        response.setVolumeCoeffV1(tariff.getVolumeCoeffV1());
        response.setVolumeCoeffV2(tariff.getVolumeCoeffV2());
        response.setVolumeCoeffV3(tariff.getVolumeCoeffV3());
        response.setIsActive(tariff.getIsActive());
        response.setEffectiveFrom(tariff.getEffectiveFrom());
        response.setEffectiveUntil(tariff.getEffectiveUntil());
        response.setCreatedAt(tariff.getCreatedAt());
        response.setUpdatedAt(tariff.getUpdatedAt());
        return response;
    }

    // Inner class for error responses
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
}
