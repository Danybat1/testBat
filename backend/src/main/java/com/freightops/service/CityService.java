package com.freightops.service;

import com.freightops.entity.City;
import com.freightops.dto.CityRequest;
import com.freightops.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * City Service
 * Business logic for City operations
 */
@Service
@Transactional
public class CityService {

    private final CityRepository cityRepository;

    @Autowired
    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    /**
     * Create a new city
     * @param cityRequest City data transfer object
     * @return created City
     */
    public City createCity(CityRequest cityRequest) {
        // Validate unique constraints
        if (cityRepository.existsByIataCodeIgnoreCase(cityRequest.getIataCode())) {
            throw new IllegalArgumentException("IATA code already exists: " + cityRequest.getIataCode());
        }
        if (cityRepository.existsByNameIgnoreCase(cityRequest.getName())) {
            throw new IllegalArgumentException("City name already exists: " + cityRequest.getName());
        }

        // Convert DTO to Entity
        City city = new City();
        city.setName(cityRequest.getName());
        city.setIataCode(cityRequest.getIataCode().toUpperCase());
        city.setCountry(cityRequest.getCountry());
        city.setIsActive(cityRequest.getIsActive() != null ? cityRequest.getIsActive() : true);

        return cityRepository.save(city);
    }

    /**
     * Get city by ID
     * @param id City ID
     * @return Optional City
     */
    @Transactional(readOnly = true)
    public Optional<City> getCityById(Long id) {
        return cityRepository.findById(id);
    }

    /**
     * Get city by IATA code
     * @param iataCode IATA code
     * @return Optional City
     */
    @Transactional(readOnly = true)
    public Optional<City> getCityByIataCode(String iataCode) {
        return cityRepository.findByIataCodeIgnoreCase(iataCode);
    }

    /**
     * Get all cities with pagination
     * @param pageable pagination information
     * @return Page of Cities
     */
    @Transactional(readOnly = true)
    public Page<City> getAllCities(Pageable pageable) {
        return cityRepository.findAll(pageable);
    }

    /**
     * Search cities by name or IATA code
     * @param query search query
     * @return List of Cities
     */
    @Transactional(readOnly = true)
    public List<City> searchCities(String query) {
        return cityRepository.findByNameContainingIgnoreCaseOrIataCodeContainingIgnoreCase(query, query);
    }

    /**
     * Update city
     * @param id City ID
     * @param cityRequest updated City data
     * @return updated City
     */
    public Optional<City> updateCity(Long id, CityRequest cityRequest) {
        Optional<City> cityOpt = cityRepository.findById(id);
        if (cityOpt.isPresent()) {
            City city = cityOpt.get();
            
            // Check unique constraints if values are being changed
            if (!city.getIataCode().equals(cityRequest.getIataCode().toUpperCase()) && 
                cityRepository.existsByIataCodeIgnoreCase(cityRequest.getIataCode())) {
                throw new IllegalArgumentException("IATA code already exists: " + cityRequest.getIataCode());
            }
            if (!city.getName().equalsIgnoreCase(cityRequest.getName()) && 
                cityRepository.existsByNameIgnoreCase(cityRequest.getName())) {
                throw new IllegalArgumentException("City name already exists: " + cityRequest.getName());
            }

            // Update fields
            city.setName(cityRequest.getName());
            city.setIataCode(cityRequest.getIataCode().toUpperCase());
            city.setCountry(cityRequest.getCountry());
            if (cityRequest.getIsActive() != null) {
                city.setIsActive(cityRequest.getIsActive());
            }

            return Optional.of(cityRepository.save(city));
        }
        return Optional.empty();
    }

    /**
     * Delete city
     * @param id City ID
     * @return true if deleted, false if not found
     */
    public boolean deleteCity(Long id) {
        if (cityRepository.existsById(id)) {
            cityRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
