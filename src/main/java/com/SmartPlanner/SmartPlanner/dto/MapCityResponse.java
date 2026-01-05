package com.SmartPlanner.SmartPlanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MapCityResponse {
    private String cityId;
    private String cityName;
    private Coordinates center;
    private BoundingBox boundingBox;
    private List<MapMarker> markers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Coordinates {
        private double latitude;
        private double longitude;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoundingBox {
        private double north;
        private double south;
        private double east;
        private double west;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MapMarker {
        private String id;
        private String name;
        private String type;
        private double latitude;
        private double longitude;
        private String description;
        private String categoryId;
    }
}