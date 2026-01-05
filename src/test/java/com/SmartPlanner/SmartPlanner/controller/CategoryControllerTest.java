package com.SmartPlanner.SmartPlanner.controller;

import com.SmartPlanner.SmartPlanner.dto.CategoryRequest;
import com.SmartPlanner.SmartPlanner.model.Category;
import com.SmartPlanner.SmartPlanner.security.JwtUtil;
import com.SmartPlanner.SmartPlanner.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller Test for CategoryController
 */
@WebMvcTest(
        controllers = CategoryController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CategoryService categoryService;

    private Category testCategory;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId("cat123");
        testCategory.setName("Fishing");
        testCategory.setDescription("Deep sea fishing");
        testCategory.setCityId("city123");
        testCategory.setPricePerHour(new BigDecimal("50.00"));
        testCategory.setPricePerDay(new BigDecimal("200.00"));
        testCategory.setImageUrl("/images/fishing.jpg");
        testCategory.setIsActive(true);

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Fishing");
        categoryRequest.setDescription("Deep sea fishing");
        categoryRequest.setCityId("city123");
        categoryRequest.setPricePerHour(new BigDecimal("50.00"));
        categoryRequest.setPricePerDay(new BigDecimal("200.00"));
        categoryRequest.setImageUrl("/images/fishing.jpg");
    }

    // ==================== PUBLIC APIs ====================

    @Test
    @DisplayName("GET /api/v1/categories - Get all categories")
    void testGetAllCategories() throws Exception {
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("cat123"))
                .andExpect(jsonPath("$[0].name").value("Fishing"));

        verify(categoryService).getAllCategories();
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} - Get category by ID - Success")
    void testGetCategoryById_Success() throws Exception {
        when(categoryService.getCategoryById("cat123")).thenReturn(testCategory);

        mockMvc.perform(get("/api/v1/categories/cat123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("cat123"))
                .andExpect(jsonPath("$.name").value("Fishing"));

        verify(categoryService).getCategoryById("cat123");
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} - Get category by ID - Not Found")
    void testGetCategoryById_NotFound() throws Exception {
        when(categoryService.getCategoryById("invalid")).thenThrow(new RuntimeException("Category not found"));

        mockMvc.perform(get("/api/v1/categories/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Category not found"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/city/{cityId} - Get categories by city")
    void testGetCategoriesByCity_Success() throws Exception {
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryService.getCategoriesByCityId("city123")).thenReturn(categories);

        mockMvc.perform(get("/api/v1/categories/city/city123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("cat123"));

        verify(categoryService).getCategoriesByCityId("city123");
    }

    @Test
    @DisplayName("GET /api/v1/categories/city/{cityId} - City not found")
    void testGetCategoriesByCity_CityNotFound() throws Exception {
        when(categoryService.getCategoriesByCityId("invalid")).thenThrow(new RuntimeException("City not found"));

        mockMvc.perform(get("/api/v1/categories/city/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("City not found"));
    }

    // ==================== ADMIN APIs ====================

    @Test
    @DisplayName("POST /api/v1/admin/categories - Add category - Success")
    void testAddCategory_Success() throws Exception {
        when(categoryService.addCategory(any(CategoryRequest.class))).thenReturn(testCategory);

        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("cat123"))
                .andExpect(jsonPath("$.name").value("Fishing"));

        verify(categoryService).addCategory(any(CategoryRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/admin/categories - Add category - Duplicate")
    void testAddCategory_Duplicate() throws Exception {
        when(categoryService.addCategory(any(CategoryRequest.class)))
                .thenThrow(new RuntimeException("Category already exists"));

        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Category already exists"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/categories - Add category - Invalid request")
    void testAddCategory_Invalid() throws Exception {
        CategoryRequest invalidRequest = new CategoryRequest();
        // Missing required fields

        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/categories/{id} - Update category - Success")
    void testUpdateCategory_Success() throws Exception {
        when(categoryService.updateCategory(eq("cat123"), any(CategoryRequest.class))).thenReturn(testCategory);

        mockMvc.perform(put("/api/v1/admin/categories/cat123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("cat123"));

        verify(categoryService).updateCategory(eq("cat123"), any(CategoryRequest.class));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/categories/{id} - Update category - Not Found")
    void testUpdateCategory_NotFound() throws Exception {
        when(categoryService.updateCategory(eq("invalid"), any(CategoryRequest.class)))
                .thenThrow(new RuntimeException("Category not found"));

        mockMvc.perform(put("/api/v1/admin/categories/invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Category not found"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/categories/{id} - Delete category - Success")
    void testDeleteCategory_Success() throws Exception {
        doNothing().when(categoryService).deleteCategory("cat123");

        mockMvc.perform(delete("/api/v1/admin/categories/cat123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));

        verify(categoryService).deleteCategory("cat123");
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/categories/{id} - Delete category - Not Found")
    void testDeleteCategory_NotFound() throws Exception {
        doThrow(new RuntimeException("Category not found")).when(categoryService).deleteCategory("invalid");

        mockMvc.perform(delete("/api/v1/admin/categories/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Category not found"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/categories/{id}/toggle - Toggle status - Success")
    void testToggleCategoryStatus_Success() throws Exception {
        testCategory.setIsActive(false);
        when(categoryService.toggleCategoryStatus("cat123")).thenReturn(testCategory);

        mockMvc.perform(patch("/api/v1/admin/categories/cat123/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        verify(categoryService).toggleCategoryStatus("cat123");
    }

    @Test
    @DisplayName("POST /api/v1/admin/categories/seed/{cityId} - Seed categories - Success")
    void testSeedCategories_Success() throws Exception {
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryService.addSampleCategories("city123")).thenReturn(categories);

        mockMvc.perform(post("/api/v1/admin/categories/seed/city123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("cat123"));

        verify(categoryService).addSampleCategories("city123");
    }

    @Test
    @DisplayName("POST /api/v1/admin/categories/seed/{cityId} - City not found")
    void testSeedCategories_CityNotFound() throws Exception {
        when(categoryService.addSampleCategories("invalid"))
                .thenThrow(new RuntimeException("City not found"));

        mockMvc.perform(post("/api/v1/admin/categories/seed/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("City not found"));
    }
}
