package com.freightops.service;

import com.freightops.entity.Tariff;
import com.freightops.entity.City;
import com.freightops.dto.TariffRequest;
import com.freightops.repository.TariffRepository;
import com.freightops.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Tariff Service
 * Business logic for Tariff operations
 */
@Service
@Transactional
public class TariffService {

    private final TariffRepository tariffRepository;
    private final CityRepository cityRepository;

    @Autowired
    public TariffService(TariffRepository tariffRepository, CityRepository cityRepository) {
        this.tariffRepository = tariffRepository;
        this.cityRepository = cityRepository;
    }

    /**
     * Create a new tariff
     * @param tariffRequest Tariff data transfer object
     * @return created Tariff
     */
    public Tariff createTariff(TariffRequest tariffRequest) {
        // Validate cities exist
        Optional<City> originCity = cityRepository.findById(tariffRequest.getOriginCityId());
        Optional<City> destinationCity = cityRepository.findById(tariffRequest.getDestinationCityId());
        
        if (originCity.isEmpty()) {
            throw new IllegalArgumentException("Origin city not found: " + tariffRequest.getOriginCityId());
        }
        if (destinationCity.isEmpty()) {
            throw new IllegalArgumentException("Destination city not found: " + tariffRequest.getDestinationCityId());
        }

        // Check if tariff already exists for this route
        if (tariffRepository.existsByOriginCityIdAndDestinationCityId(tariffRequest.getOriginCityId(), tariffRequest.getDestinationCityId())) {
            throw new IllegalArgumentException("Tariff already exists for this route");
        }

        // Convert DTO to Entity
        Tariff tariff = new Tariff();
        tariff.setOriginCity(originCity.get());
        tariff.setDestinationCity(destinationCity.get());
        tariff.setKgRate(tariffRequest.getKgRate());
        tariff.setVolumeCoeffV1(tariffRequest.getVolumeCoeffV1());
        tariff.setVolumeCoeffV2(tariffRequest.getVolumeCoeffV2());
        tariff.setVolumeCoeffV3(tariffRequest.getVolumeCoeffV3());
        tariff.setIsActive(tariffRequest.getIsActive() != null ? tariffRequest.getIsActive() : true);
        tariff.setEffectiveFrom(tariffRequest.getEffectiveFrom());
        tariff.setEffectiveUntil(tariffRequest.getEffectiveUntil());

        return tariffRepository.save(tariff);
    }

    /**
     * Get tariff by ID
     * @param id Tariff ID
     * @return Optional Tariff
     */
    @Transactional(readOnly = true)
    public Optional<Tariff> getTariffById(Long id) {
        return tariffRepository.findById(id);
    }

    /**
     * Get tariff by route (origin and destination cities)
     * @param originCityId Origin city ID
     * @param destinationCityId Destination city ID
     * @return Optional Tariff
     */
    @Transactional(readOnly = true)
    public Optional<Tariff> getTariffByRoute(Long originCityId, Long destinationCityId) {
        Optional<City> originCity = cityRepository.findById(originCityId);
        Optional<City> destinationCity = cityRepository.findById(destinationCityId);
        
        if (originCity.isEmpty() || destinationCity.isEmpty()) {
            return Optional.empty();
        }
        
        return tariffRepository.findByOriginAndDestination(originCityId, destinationCityId, java.time.LocalDateTime.now());
    }

    /**
     * Get all tariffs with pagination
     * @param pageable pagination information
     * @return Page of Tariffs
     */
    @Transactional(readOnly = true)
    public Page<Tariff> getAllTariffs(Pageable pageable) {
        return tariffRepository.findAll(pageable);
    }

    /**
     * Update tariff
     * @param id Tariff ID
     * @param tariffRequest updated Tariff data
     * @return updated Tariff
     */
    public Optional<Tariff> updateTariff(Long id, TariffRequest tariffRequest) {
        Optional<Tariff> tariffOpt = tariffRepository.findById(id);
        if (tariffOpt.isPresent()) {
            Tariff tariff = tariffOpt.get();
            
            // Validate cities exist if they are being changed
            Optional<City> originCity = cityRepository.findById(tariffRequest.getOriginCityId());
            Optional<City> destinationCity = cityRepository.findById(tariffRequest.getDestinationCityId());
            
            if (originCity.isEmpty()) {
                throw new IllegalArgumentException("Origin city not found: " + tariffRequest.getOriginCityId());
            }
            if (destinationCity.isEmpty()) {
                throw new IllegalArgumentException("Destination city not found: " + tariffRequest.getDestinationCityId());
            }

            // Check if route is being changed and if new route already exists
            if (!tariff.getOriginCity().getId().equals(tariffRequest.getOriginCityId()) ||
                !tariff.getDestinationCity().getId().equals(tariffRequest.getDestinationCityId())) {
                if (tariffRepository.existsByOriginCityIdAndDestinationCityIdAndIdNot(tariffRequest.getOriginCityId(), tariffRequest.getDestinationCityId(), id)) {
                    throw new IllegalArgumentException("Tariff already exists for this route");
                }
            }

            // Update fields
            tariff.setOriginCity(originCity.get());
            tariff.setDestinationCity(destinationCity.get());
            tariff.setKgRate(tariffRequest.getKgRate());
            tariff.setVolumeCoeffV1(tariffRequest.getVolumeCoeffV1());
            tariff.setVolumeCoeffV2(tariffRequest.getVolumeCoeffV2());
            tariff.setVolumeCoeffV3(tariffRequest.getVolumeCoeffV3());
            if (tariffRequest.getIsActive() != null) {
                tariff.setIsActive(tariffRequest.getIsActive());
            }
            tariff.setEffectiveFrom(tariffRequest.getEffectiveFrom());
            tariff.setEffectiveUntil(tariffRequest.getEffectiveUntil());

            return Optional.of(tariffRepository.save(tariff));
        }
        return Optional.empty();
    }

    /**
     * Delete tariff
     * @param id Tariff ID
     * @return true if deleted, false if not found
     */
    public boolean deleteTariff(Long id) {
        if (tariffRepository.existsById(id)) {
            tariffRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
