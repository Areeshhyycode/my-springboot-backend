package com.SmartPlanner.SmartPlanner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * FULL COUNTRY REQUEST - Ek API mein Country + Cities + Activities add karo
 *
 * Example:
 * {
 *   "name": "United Arab Emirates",
 *   "code": "UAE",
 *   "cities": [
 *     {
 *       "name": "Dubai",
 *       "activities": [
 *         {"name": "Beach", "price": 50, "duration": 3},
 *         {"name": "Desert Safari", "price": 100, "duration": 5}
 *       ]
 *     }
 *   ]
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FullCountryRequest {

    @NotBlank(message = "Country name is required")
    private String name;

    private String code;
    private String imageUrl;
    private String description;

    @NotEmpty(message = "At least one city is required")
    private List<CityData> cities;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CityData {
        @NotBlank(message = "City name is required")
        private String name;
        private String imageUrl;
        private String description;

        private List<ActivityData> activities;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityData {
        @NotBlank(message = "Activity name is required")
        private String name;
        private String description;
        private BigDecimal pricePerHour;   // Price per hour
        private BigDecimal pricePerDay;    // Price per day
        private String imageUrl;
    }
}
