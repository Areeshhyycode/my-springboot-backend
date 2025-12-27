package com.SmartPlanner.SmartPlanner.repository;

import com.SmartPlanner.SmartPlanner.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CATEGORY REPOSITORY - Database operations for categories
 */
@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    // City ke hisab se categories find karo
    List<Category> findByCityId(String cityId);

    // Active categories find karo by city
    List<Category> findByCityIdAndIsActiveTrue(String cityId);

    // Category name se find karo
    List<Category> findByNameContainingIgnoreCase(String name);

    // Check category exists by name in a city
    boolean existsByNameIgnoreCaseAndCityId(String name, String cityId);
}
