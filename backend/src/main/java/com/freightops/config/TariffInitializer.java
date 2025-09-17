package com.freightops.config;

import com.freightops.entity.City;
import com.freightops.entity.Tariff;
import com.freightops.repository.CityRepository;
import com.freightops.repository.TariffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tariff Initializer for DRC Routes
 * Populates the database with realistic tariffs between DRC cities
 */
@Component
@Order(2) // Execute after DataInitializer
public class TariffInitializer implements CommandLineRunner {

    private final TariffRepository tariffRepository;
    private final CityRepository cityRepository;

    @Autowired
    public TariffInitializer(TariffRepository tariffRepository, CityRepository cityRepository) {
        this.tariffRepository = tariffRepository;
        this.cityRepository = cityRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        initializeDRCTariffs();
    }

    private void initializeDRCTariffs() {
        // Check if tariffs already exist to avoid duplicates
        if (tariffRepository.count() > 0) {
            return;
        }

        // Get all cities
        List<City> cities = cityRepository.findAll();
        if (cities.isEmpty())
            return;

        // Define tariff rates based on distance and route importance
        // Rates are in USD per kg
        createTariffsBetweenCities(cities);
    }

    private void createTariffsBetweenCities(List<City> cities) {
        // Major routes with competitive rates
        createTariff("Kinshasa", "Lubumbashi", new BigDecimal("2.50"), cities); // Capital to mining center
        createTariff("Kinshasa", "Kisangani", new BigDecimal("2.20"), cities); // Capital to northern hub
        createTariff("Kinshasa", "Mbuji-Mayi", new BigDecimal("2.30"), cities); // Capital to diamond center
        createTariff("Kinshasa", "Goma", new BigDecimal("2.80"), cities); // Capital to eastern border
        createTariff("Kinshasa", "Kananga", new BigDecimal("2.10"), cities); // Capital to Kasai
        createTariff("Kinshasa", "Bukavu", new BigDecimal("2.90"), cities); // Capital to lake region
        createTariff("Kinshasa", "Matadi", new BigDecimal("1.80"), cities); // Capital to port
        createTariff("Kinshasa", "Mbandaka", new BigDecimal("2.00"), cities); // Capital to river port

        // Lubumbashi regional routes
        createTariff("Lubumbashi", "Kolwezi", new BigDecimal("1.50"), cities); // Mining corridor
        createTariff("Lubumbashi", "Likasi", new BigDecimal("1.20"), cities); // Short mining route
        createTariff("Lubumbashi", "Kisangani", new BigDecimal("2.60"), cities); // Cross-country
        createTariff("Lubumbashi", "Mbuji-Mayi", new BigDecimal("2.40"), cities);// Mining to diamond
        createTariff("Lubumbashi", "Goma", new BigDecimal("2.70"), cities); // Mining to east
        createTariff("Lubumbashi", "Bukavu", new BigDecimal("2.80"), cities); // Mining to lake

        // Eastern region routes
        createTariff("Goma", "Bukavu", new BigDecimal("1.60"), cities); // Lake region
        createTariff("Goma", "Beni", new BigDecimal("1.40"), cities); // Border region
        createTariff("Goma", "Bunia", new BigDecimal("1.80"), cities); // Eastern mining
        createTariff("Bukavu", "Uvira", new BigDecimal("1.30"), cities); // Lake Tanganyika

        // Kasai region routes
        createTariff("Mbuji-Mayi", "Kananga", new BigDecimal("1.70"), cities); // Diamond region
        createTariff("Mbuji-Mayi", "Tshikapa", new BigDecimal("1.90"), cities); // Diamond corridor
        createTariff("Kananga", "Tshikapa", new BigDecimal("1.60"), cities); // Kasai internal

        // Northern routes
        createTariff("Kisangani", "Isiro", new BigDecimal("2.10"), cities); // Northern corridor
        createTariff("Kisangani", "Bunia", new BigDecimal("2.30"), cities); // North to east
        createTariff("Kisangani", "Mbandaka", new BigDecimal("2.40"), cities); // River network

        // Western routes
        createTariff("Matadi", "Boma", new BigDecimal("1.20"), cities); // Coastal route
        createTariff("Matadi", "Kikwit", new BigDecimal("2.00"), cities); // Port to interior
        createTariff("Mbandaka", "Gemena", new BigDecimal("1.80"), cities); // River network

        // Additional regional connections
        createTariff("Kikwit", "Bandundu", new BigDecimal("1.50"), cities); // Western interior
        createTariff("Kolwezi", "Likasi", new BigDecimal("1.10"), cities); // Mining belt
        createTariff("Isiro", "Bunia", new BigDecimal("1.70"), cities); // Northeast

        // Reverse routes (bidirectional)
        createReverseRoutes(cities);
    }

    private void createTariff(String originName, String destinationName, BigDecimal kgRate, List<City> cities) {
        City origin = findCityByName(originName, cities);
        City destination = findCityByName(destinationName, cities);

        if (origin != null && destination != null) {
            Tariff tariff = new Tariff();
            tariff.setOriginCity(origin);
            tariff.setDestinationCity(destination);
            tariff.setKgRate(kgRate);
            tariff.setVolumeCoeffV1(new BigDecimal("1.0")); // Volume coefficient 1
            tariff.setVolumeCoeffV2(new BigDecimal("1.2")); // Volume coefficient 2
            tariff.setVolumeCoeffV3(new BigDecimal("1.5")); // Volume coefficient 3

            try {
                tariffRepository.save(tariff);
            } catch (Exception e) {
                System.err.println(
                        "Error creating tariff " + originName + " -> " + destinationName + ": " + e.getMessage());
            }
        }
    }

    private void createReverseRoutes(List<City> cities) {
        // Create reverse routes for all existing tariffs
        List<Tariff> existingTariffs = tariffRepository.findAll();

        for (Tariff tariff : existingTariffs) {
            // Check if reverse route already exists
            boolean reverseExists = tariffRepository.existsByOriginCityIdAndDestinationCityId(
                    tariff.getDestinationCity().getId(), tariff.getOriginCity().getId());

            if (!reverseExists) {
                Tariff reverseTariff = new Tariff();
                reverseTariff.setOriginCity(tariff.getDestinationCity());
                reverseTariff.setDestinationCity(tariff.getOriginCity());
                reverseTariff.setKgRate(tariff.getKgRate()); // Same rate for reverse
                reverseTariff.setVolumeCoeffV1(tariff.getVolumeCoeffV1());
                reverseTariff.setVolumeCoeffV2(tariff.getVolumeCoeffV2());
                reverseTariff.setVolumeCoeffV3(tariff.getVolumeCoeffV3());

                try {
                    tariffRepository.save(reverseTariff);
                } catch (Exception e) {
                    System.err.println("Error creating reverse tariff: " + e.getMessage());
                }
            }
        }
    }

    private City findCityByName(String name, List<City> cities) {
        return cities.stream()
                .filter(city -> city.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
