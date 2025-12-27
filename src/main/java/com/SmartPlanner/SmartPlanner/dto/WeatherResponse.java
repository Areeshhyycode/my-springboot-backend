package com.SmartPlanner.SmartPlanner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * WEATHER RESPONSE DTO - Frontend ko bhejne ke liye clean response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {

    private String cityName;
    private String country;
    private CurrentWeather current;
    private List<HourlyWeather> hourly;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentWeather {
        private String time;
        private Double temperature;      // °C
        private Double windSpeed;        // km/h
        private String temperatureUnit;  // "°C"
        private String windSpeedUnit;    // "km/h"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyWeather {
        private String time;
        private Double temperature;
        private Integer humidity;
        private Double windSpeed;
    }
}