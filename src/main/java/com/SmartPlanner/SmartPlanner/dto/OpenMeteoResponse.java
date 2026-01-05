package com.SmartPlanner.SmartPlanner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
@Data
public class OpenMeteoResponse {
    private double latitude;
    private double longitude;

    @JsonProperty("current_units")
    private CurrentUnits currentUnits;

    @JsonProperty("current")
    private CurrentWeather current;

    @JsonProperty("hourly_units")
    private HourlyUnits hourlyUnits;

    @JsonProperty("hourly")
    private HourlyWeather hourly;

    @JsonProperty("daily_units")
    private DailyUnits dailyUnits;

    @JsonProperty("daily")
    private DailyWeather daily;

    @Data
    public static class CurrentUnits {
        private String time;
        @JsonProperty("temperature_2m")
        private String temperature2m;
        @JsonProperty("wind_speed_10m")
        private String windSpeed10m;
        @JsonProperty("relative_humidity_2m")
        private String relativeHumidity2m;
        @JsonProperty("weather_code")
        private String weatherCode;
    }

    @Data
    public static class CurrentWeather {
        private String time;
        @JsonProperty("temperature_2m")
        private Double temperature2m;
        @JsonProperty("wind_speed_10m")
        private Double windSpeed10m;
        @JsonProperty("relative_humidity_2m")
        private Integer relativeHumidity2m;
        @JsonProperty("weather_code")
        private Integer weatherCode;
    }

    @Data
    public static class HourlyUnits {
        private String time;
        @JsonProperty("temperature_2m")
        private String temperature2m;
        @JsonProperty("relative_humidity_2m")
        private String relativeHumidity2m;
        @JsonProperty("wind_speed_10m")
        private String windSpeed10m;
    }

    @Data
    public static class HourlyWeather {
        private List<String> time;
        @JsonProperty("temperature_2m")
        private List<Double> temperature2m;
        @JsonProperty("relative_humidity_2m")
        private List<Integer> relativeHumidity2m;
        @JsonProperty("wind_speed_10m")
        private List<Double> windSpeed10m;
    }

    @Data
    public static class DailyUnits {
        private String time;
        @JsonProperty("temperature_2m_max")
        private String temperature2mMax;
        @JsonProperty("temperature_2m_min")
        private String temperature2mMin;
        @JsonProperty("weather_code")
        private String weatherCode;
        @JsonProperty("wind_speed_10m_max")
        private String windSpeed10mMax;
        @JsonProperty("precipitation_sum")
        private String precipitationSum; // ADD THIS
        @JsonProperty("relative_humidity_2m_max")
        private String relativeHumidity2mMax;
    }

    @Data
    public static class DailyWeather {
        private List<String> time;
        @JsonProperty("temperature_2m_max")
        private List<Double> temperature2mMax;
        @JsonProperty("temperature_2m_min")
        private List<Double> temperature2mMin;
        @JsonProperty("weather_code")
        private List<Integer> weatherCode;
        @JsonProperty("wind_speed_10m_max")
        private List<Double> windSpeed10mMax;
        @JsonProperty("precipitation_sum")
        private List<Double> precipitationSum; // ADD THIS
        @JsonProperty("relative_humidity_2m_max")
        private List<Integer> relativeHumidity2mMax;
    }
}