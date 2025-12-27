package com.SmartPlanner.SmartPlanner.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * CITY MODEL - Belongs to a Country
 *
 * Hierarchy:
 * Country (UAE)
 *   └── City (Dubai) ← This model
 *         └── Activity (Beach, Safari)
 *
 * Admin sirf city name dega, backend automatically:
 * 1. Latitude/Longitude fetch karega (Geocoding API)
 * 2. Weather fetch karega (Open-Meteo API)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cities")
public class City {

    @Id
    private String id;

    // Country reference
    private String countryId;      // Reference to Country
    private String countryName;    // Denormalized for easy access

    private String name;           // City name: "Dubai"
    private Double latitude;       // Auto-fetched from Geocoding API
    private Double longitude;      // Auto-fetched from Geocoding API
    private String imageUrl;       // City image URL (optional)
    private String description;    // Short description (optional)
    private Boolean isActive = true;

    // Embedded Weather Data (auto-fetched)
    private CityWeather weather;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime weatherUpdatedAt;

    /**
     * Embedded Weather Object
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CityWeather {
        private Double temperature;      // Current temperature in °C
        private Double windSpeed;        // Wind speed in km/h
        private Integer humidity;        // Humidity %
        private String weatherCode;      // Weather condition code
        private String description;      // "Sunny", "Cloudy", etc.
    }
}
