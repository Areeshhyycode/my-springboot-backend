package com.SmartPlanner.SmartPlanner.service;

import com.SmartPlanner.SmartPlanner.dto.CategoryRequest;
import com.SmartPlanner.SmartPlanner.model.Category;
import com.SmartPlanner.SmartPlanner.repository.CategoryRepository;
import com.SmartPlanner.SmartPlanner.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    public List<Category> getCategoriesByCityId(String cityId) {
        if (!cityRepository.existsById(cityId)) {
            throw new RuntimeException("City not found with id: " + cityId);
        }
        return categoryRepository.findByCityIdAndIsActiveTrue(cityId);
    }

    public Category addCategory(CategoryRequest request) {
        if (!cityRepository.existsById(request.getCityId())) {
            throw new RuntimeException("City not found with id: " + request.getCityId());
        }

        if (categoryRepository.existsByNameIgnoreCaseAndCityId(request.getName(), request.getCityId())) {
            throw new RuntimeException("Category already exists in this city: " + request.getName());
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setCityId(request.getCityId());
        category.setPricePerHour(request.getPricePerHour());
        category.setPricePerDay(request.getPricePerDay());
        category.setImageUrl(request.getImageUrl());
        category.setIsActive(true);

        return categoryRepository.save(category);
    }

    public Category updateCategory(String id, CategoryRequest request) {
        Category category = getCategoryById(id);

        if (!cityRepository.existsById(request.getCityId())) {
            throw new RuntimeException("City not found with id: " + request.getCityId());
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setCityId(request.getCityId());
        category.setPricePerHour(request.getPricePerHour());
        category.setPricePerDay(request.getPricePerDay());
        category.setImageUrl(request.getImageUrl());

        return categoryRepository.save(category);
    }

    public void deleteCategory(String id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    public Category toggleCategoryStatus(String id) {
        Category category = getCategoryById(id);
        category.setIsActive(!category.getIsActive());
        return categoryRepository.save(category);
    }

    public List<Category> addSampleCategories(String cityId) {
        if (!cityRepository.existsById(cityId)) {
            throw new RuntimeException("City not found with id: " + cityId);
        }

        List<Category> sampleCategories = List.of(
            new Category(null, "Fishing", "Deep sea fishing experience", cityId, new BigDecimal("40.00"), new BigDecimal("150.00"), "/images/fishing.jpg", null, null, true),
            new Category(null, "Boating", "Boat ride and tour", cityId, new BigDecimal("50.00"), new BigDecimal("200.00"), "/images/boating.jpg", null, null, true),
            new Category(null, "Sea Food", "Fresh seafood dining", cityId, new BigDecimal("25.00"), new BigDecimal("75.00"), "/images/seafood.jpg", null, null, true),
            new Category(null, "Snorkeling", "Underwater adventure", cityId, new BigDecimal("35.00"), new BigDecimal("120.00"), "/images/snorkeling.jpg", null, null, true),
            new Category(null, "Beach Resort", "Luxury beach stay", cityId, new BigDecimal("50.00"), new BigDecimal("300.00"), "/images/resort.jpg", null, null, true)
        );

        for (Category category : sampleCategories) {
            if (!categoryRepository.existsByNameIgnoreCaseAndCityId(category.getName(), cityId)) {
                categoryRepository.save(category);
            }
        }

        return categoryRepository.findByCityId(cityId);
    }
}
