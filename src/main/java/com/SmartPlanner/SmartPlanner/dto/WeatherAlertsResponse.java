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
public class WeatherAlertsResponse {
    private String cityName;
    private String country;
    private CurrentConditions currentConditions;
    private List<WeatherAlert> alerts;
    private WeatherStatus status;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CurrentConditions {
        private double temperature;
        private double windSpeed;
        private int humidity;
        private String weatherDescription;
        private String time;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WeatherAlert {
        private String type;
        private String severity;  // low, medium, high, extreme
        private String title;
        private String message;
        private String icon;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WeatherStatus {
        private String level;  // safe, caution, warning, danger
        private String message;
        private String color;
    }
}
