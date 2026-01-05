package com.SmartPlanner.SmartPlanner.repository;

import com.SmartPlanner.SmartPlanner.model.City;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends MongoRepository<City, String> {

    Optional<City> findByNameIgnoreCase(String name);

    List<City> findByCountryId(String countryId);

    List<City> findByCountryIdAndIsActiveTrue(String countryId);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndCountryId(String name, String countryId);

    List<City> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
}
