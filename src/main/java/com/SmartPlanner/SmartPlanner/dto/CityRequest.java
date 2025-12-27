package com.SmartPlanner.SmartPlanner.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CITY REQUEST DTO - Add city under a country
 *
 * Admin provides:
 * - countryId (required)
 * - name (required)
 *
 * Backend automatically fetches:
 * - Latitude/Longitude (Geocoding API)
 * - Weather (Open-Meteo API)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityRequest {

    @NotBlank(message = "Country ID is required")
    private String countryId;  // Parent country

    @NotBlank(message = "City name is required")
    private String name;  // City name: "Dubai"

    // Optional fields
    private String imageUrl;
    private String description;
}
