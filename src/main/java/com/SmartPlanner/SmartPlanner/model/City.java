package com.SmartPlanner.SmartPlanner.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cities")
public class City {

    @Id
    private String id;

    private String countryId;
    private String countryName;

    private String name;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private String description;
    private Boolean isActive = true;

    private CityWeather weather;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime weatherUpdatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CityWeather {
        private Double temperature;
        private Double windSpeed;
        private Integer humidity;
        private String weatherCode;
        private String description;
    }
}
