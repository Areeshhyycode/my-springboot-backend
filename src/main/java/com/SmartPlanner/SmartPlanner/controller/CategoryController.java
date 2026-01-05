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

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/api/v1/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/api/v1/categories/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(categoryService.getCategoryById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/api/v1/categories/city/{cityId}")
    public ResponseEntity<?> getCategoriesByCity(@PathVariable String cityId) {
        try {
            return ResponseEntity.ok(categoryService.getCategoriesByCityId(cityId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/api/v1/admin/categories")
    public ResponseEntity<?> addCategory(@Valid @RequestBody CategoryRequest request) {
        try {
            Category category = categoryService.addCategory(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(category);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/api/v1/admin/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable String id, @Valid @RequestBody CategoryRequest request) {
        try {
            Category category = categoryService.updateCategory(id, request);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/api/v1/admin/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable String id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(new SuccessResponse("Category deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PatchMapping("/api/v1/admin/categories/{id}/toggle")
    public ResponseEntity<?> toggleCategoryStatus(@PathVariable String id) {
        try {
            Category category = categoryService.toggleCategoryStatus(id);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/api/v1/admin/categories/seed/{cityId}")
    public ResponseEntity<?> seedCategories(@PathVariable String cityId) {
        try {
            return ResponseEntity.ok(categoryService.addSampleCategories(cityId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    record ErrorResponse(String message) {}
    record SuccessResponse(String message) {}
}
