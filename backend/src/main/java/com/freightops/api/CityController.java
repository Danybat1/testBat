package com.freightops.api;

import com.freightops.entity.City;
import com.freightops.dto.CityRequest;
import com.freightops.dto.CityResponse;
import com.freightops.service.CityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * City REST Controller
 * Handles HTTP requests for City operations
 */
@RestController
@RequestMapping("/api/city")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CityController {

    private final CityService cityService;

    @Autowired
    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    /**
     * Create a new city
     * POST /api/city
     */
    @PostMapping
    public ResponseEntity<?> createCity(@Valid @RequestBody CityRequest cityRequest) {
        try {
            City createdCity = cityService.createCity(cityRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponse(createdCity));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to create city: " + e.getMessage()));
        }
    }

    /**
     * Get all cities with pagination
     * GET /api/city
     */
    @GetMapping
    public ResponseEntity<Page<CityResponse>> getAllCities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<City> cityPage = cityService.getAllCities(pageable);
        Page<CityResponse> cityResponsePage = cityPage.map(this::convertToResponse);
        return ResponseEntity.ok(cityResponsePage);
    }

    /**
     * Search cities by name or IATA code
     * GET /api/city/search
     */
    @GetMapping("/search")
    public ResponseEntity<List<CityResponse>> searchCities(@RequestParam String query) {
        List<City> cities = cityService.searchCities(query);
        List<CityResponse> cityResponses = cities.stream()
                .map(this::convertToResponse)
                .toList();
        return ResponseEntity.ok(cityResponses);
    }

    /**
     * Get city by ID
     * GET /api/city/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCityById(@PathVariable Long id) {
        Optional<City> cityOpt = cityService.getCityById(id);
        if (cityOpt.isPresent()) {
            return ResponseEntity.ok(convertToResponse(cityOpt.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get city by IATA code
     * GET /api/city/iata/{iataCode}
     */
    @GetMapping("/iata/{iataCode}")
    public ResponseEntity<?> getCityByIataCode(@PathVariable String iataCode) {
        Optional<City> cityOpt = cityService.getCityByIataCode(iataCode);
        if (cityOpt.isPresent()) {
            return ResponseEntity.ok(convertToResponse(cityOpt.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update city
     * PUT /api/city/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCity(@PathVariable Long id, @Valid @RequestBody CityRequest cityRequest) {
        try {
            Optional<City> updatedCity = cityService.updateCity(id, cityRequest);
            if (updatedCity.isPresent()) {
                return ResponseEntity.ok(convertToResponse(updatedCity.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to update city: " + e.getMessage()));
        }
    }

    /**
     * Delete city
     * DELETE /api/city/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCity(@PathVariable Long id) {
        try {
            boolean deleted = cityService.deleteCity(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to delete city: " + e.getMessage()));
        }
    }

    /**
     * Convert City entity to Response DTO
     */
    private CityResponse convertToResponse(City city) {
        CityResponse response = new CityResponse();
        response.setId(city.getId());
        response.setName(city.getName());
        response.setIataCode(city.getIataCode());
        response.setCountry(city.getCountry());
        response.setIsActive(city.getIsActive());
        response.setCreatedAt(city.getCreatedAt());
        response.setUpdatedAt(city.getUpdatedAt());
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
