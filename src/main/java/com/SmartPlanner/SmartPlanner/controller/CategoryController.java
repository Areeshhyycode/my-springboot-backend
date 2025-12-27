package com.SmartPlanner.SmartPlanner.controller;

import com.SmartPlanner.SmartPlanner.dto.CategoryRequest;
import com.SmartPlanner.SmartPlanner.model.Category;
import com.SmartPlanner.SmartPlanner.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CATEGORY CONTROLLER - Category APIs
 *
 * Base URL: /api/v1/categories
 *
 * PUBLIC APIs (All users):
 *   - GET /api/v1/categories - Get all categories
 *   - GET /api/v1/categories/{id} - Get category by ID
 *   - GET /api/v1/categories/city/{cityId} - Get categories by city
 *
 * ADMIN ONLY APIs:
 *   - POST /api/v1/admin/categories - Add category
 *   - PUT /api/v1/admin/categories/{id} - Update category
 *   - DELETE /api/v1/admin/categories/{id} - Delete category
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    // ==================== PUBLIC APIs (All Users) ====================

    /**
     * GET ALL CATEGORIES
     * URL: GET /api/v1/categories
     */
    @GetMapping("/api/v1/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    /**
     * GET CATEGORY BY ID
     * URL: GET /api/v1/categories/{id}
     */
    @GetMapping("/api/v1/categories/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(categoryService.getCategoryById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET CATEGORIES BY CITY
     * URL: GET /api/v1/categories/city/{cityId}
     *
     * Jab user city card click kare, ye API call hogi
     */
    @GetMapping("/api/v1/categories/city/{cityId}")
    public ResponseEntity<?> getCategoriesByCity(@PathVariable String cityId) {
        try {
            return ResponseEntity.ok(categoryService.getCategoriesByCityId(cityId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ==================== ADMIN ONLY APIs ====================

    /**
     * ADD NEW CATEGORY (Admin Only)
     *
     * URL: POST /api/v1/admin/categories
     *
     * Request Body:
     * {
     *   "name": "Fishing",
     *   "description": "Deep sea fishing",
     *   "cityId": "676abc123...",
     *   "price": 150.00,
     *   "imageUrl": "/images/fishing.jpg",
     *   "duration": 4
     * }
     */
    @PostMapping("/api/v1/admin/categories")
    public ResponseEntity<?> addCategory(@Valid @RequestBody CategoryRequest request) {
        try {
            Category category = categoryService.addCategory(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(category);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * UPDATE CATEGORY (Admin Only)
     * URL: PUT /api/v1/admin/categories/{id}
     */
    @PutMapping("/api/v1/admin/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable String id, @Valid @RequestBody CategoryRequest request) {
        try {
            Category category = categoryService.updateCategory(id, request);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * DELETE CATEGORY (Admin Only)
     * URL: DELETE /api/v1/admin/categories/{id}
     */
    @DeleteMapping("/api/v1/admin/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable String id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(new SuccessResponse("Category deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * TOGGLE CATEGORY STATUS (Admin Only)
     * URL: PATCH /api/v1/admin/categories/{id}/toggle
     */
    @PatchMapping("/api/v1/admin/categories/{id}/toggle")
    public ResponseEntity<?> toggleCategoryStatus(@PathVariable String id) {
        try {
            Category category = categoryService.toggleCategoryStatus(id);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * ADD SAMPLE CATEGORIES (Admin Only)
     * URL: POST /api/v1/admin/categories/seed/{cityId}
     */
    @PostMapping("/api/v1/admin/categories/seed/{cityId}")
    public ResponseEntity<?> seedCategories(@PathVariable String cityId) {
        try {
            return ResponseEntity.ok(categoryService.addSampleCategories(cityId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // Response records
    record ErrorResponse(String message) {}
    record SuccessResponse(String message) {}
}
