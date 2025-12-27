package com.SmartPlanner.SmartPlanner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * CATEGORY REQUEST DTO - New category add karne ke liye
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;

    @NotBlank(message = "City ID is required")
    private String cityId;

    @Positive(message = "Price per hour must be positive")
    private BigDecimal pricePerHour;   // Price per hour

    @Positive(message = "Price per day must be positive")
    private BigDecimal pricePerDay;    // Price per day

    private String imageUrl;
}
