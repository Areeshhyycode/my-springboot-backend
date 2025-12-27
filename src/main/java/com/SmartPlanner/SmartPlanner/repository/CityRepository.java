package com.SmartPlanner.SmartPlanner.repository;

import com.SmartPlanner.SmartPlanner.model.City;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CITY REPOSITORY - Database operations for cities
 */
@Repository
public interface CityRepository extends MongoRepository<City, String> {

    // City name se find karo
    Optional<City> findByNameIgnoreCase(String name);

    // Country ID se cities find karo
    List<City> findByCountryId(String countryId);

    // Active cities by country
    List<City> findByCountryIdAndIsActiveTrue(String countryId);

    // Check city exists by name
    boolean existsByNameIgnoreCase(String name);

    // Check city exists by name in a specific country
    boolean existsByNameIgnoreCaseAndCountryId(String name, String countryId);

    // Search by name (partial match, case insensitive)
    List<City> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
}
