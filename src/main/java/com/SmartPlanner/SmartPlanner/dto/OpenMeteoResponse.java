package com.SmartPlanner.SmartPlanner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * OPEN-METEO API RESPONSE - External API ka response map karne ke liye
 */
@Data
public class OpenMeteoResponse {

    private Double latitude;
    private Double longitude;
    private String timezone;
    private Double elevation;

    private Current current;
    private Hourly hourly;

    @Data
    public static class Current {
        private String time;

        @JsonProperty("temperature_2m")
        private Double temperature;

        @JsonProperty("wind_speed_10m")
        private Double windSpeed;

        @JsonProperty("relative_humidity_2m")
        private Integer humidity;

        @JsonProperty("weather_code")
        private Integer weatherCode;
    }

    @Data
    public static class Hourly {
        private List<String> time;

        @JsonProperty("temperature_2m")
        private List<Double> temperature;

        @JsonProperty("relative_humidity_2m")
        private List<Integer> humidity;

        @JsonProperty("wind_speed_10m")
        private List<Double> windSpeed;
    }
}