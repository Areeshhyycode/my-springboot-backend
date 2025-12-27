package com.SmartPlanner.SmartPlanner.dto;

import com.SmartPlanner.SmartPlanner.model.City;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * SEARCH RESPONSE - Search results with weather
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {

    private String query;
    private int totalResults;
    private List<SearchResult> results;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResult {
        private String type;           // "COUNTRY" or "CITY"
        private String id;
        private String name;
        private String description;
        private String imageUrl;

        // For City
        private String countryId;
        private String countryName;
        private Double latitude;
        private Double longitude;
        private City.CityWeather weather;

        // Activities (for city)
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
        private BigDecimal pricePerHour;
        private BigDecimal pricePerDay;
        private String imageUrl;
    }
}
