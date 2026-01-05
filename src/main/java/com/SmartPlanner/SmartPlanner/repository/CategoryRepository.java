package com.SmartPlanner.SmartPlanner.repository;

import com.SmartPlanner.SmartPlanner.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    List<Category> findByCityId(String cityId);

    List<Category> findByCityIdAndIsActiveTrue(String cityId);

    List<Category> findByNameContainingIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndCityId(String name, String cityId);
}
