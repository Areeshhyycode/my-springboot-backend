package com.SmartPlanner.SmartPlanner.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PackingSuggestionsResponse {
    private String cityName;
    private String country;
    private WeatherSummary weatherSummary;
    private List<PackingCategory> categories;
    private List<String> generalTips;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WeatherSummary {
        private double avgTemperature;
        private double maxTemperature;
        private double minTemperature;
        private int avgHumidity;
        private double maxWindSpeed;
        private String dominantWeather;
        private String period;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PackingCategory {
        private String category;
        private String icon;
        private List<PackingItem> items;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PackingItem {
        private String name;
        private String reason;
        private String priority;  // essential, recommended, optional
        private String icon;
    }
}