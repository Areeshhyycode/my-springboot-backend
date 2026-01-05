package com.SmartPlanner.SmartPlanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearbyPlacesResponse {
    private Coordinates center;
    private int radius;
    private String type;
    private List<Place> places;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Place {
        private String id;
        private String name;
        private String type;
        private double latitude;
        private double longitude;
        private String address;
        private String distance;
    }
}