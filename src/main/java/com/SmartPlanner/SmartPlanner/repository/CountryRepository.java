package com.SmartPlanner.SmartPlanner.repository;

import com.SmartPlanner.SmartPlanner.model.Country;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends MongoRepository<Country, String> {

    Optional<Country> findByNameIgnoreCase(String name);

    Optional<Country> findByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByCodeIgnoreCase(String code);

    List<Country> findByIsActiveTrue();

    List<Country> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
}
