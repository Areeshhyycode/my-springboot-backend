package com.SmartPlanner.SmartPlanner.dto;

import com.SmartPlanner.SmartPlanner.model.City;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * FULL COUNTRY RESPONSE - Frontend ke liye nested response
 *
 * Country
 *   └── Cities (with weather)
 *         └── Activities
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FullCountryResponse {

    private String id;
    private String name;
    private String code;
    private String imageUrl;
    private String description;
    private List<CityWithActivities> cities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CityWithActivities {
        private String id;
        private String name;
        private Double latitude;
        private Double longitude;
        private String imageUrl;
        private String description;
        private City.CityWeather weather;
        private List<ActivityInfo> activities;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityInfo {
        private String id;
        private String name;
        private String description;
        private BigDecimal pricePerHour;   // Price per hour
        private BigDecimal pricePerDay;    // Price per day
        private String imageUrl;
    }
}
