package com.SmartPlanner.SmartPlanner.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COUNTRY REQUEST DTO - Add/Update country
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountryRequest {

    @NotBlank(message = "Country name is required")
    private String name;  // "United Arab Emirates"

    private String code;  // "UAE" or "AE" (optional, auto-generated if not provided)

    private String imageUrl;
    private String description;
}
