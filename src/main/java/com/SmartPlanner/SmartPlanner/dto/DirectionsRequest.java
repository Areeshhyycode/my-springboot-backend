package com.SmartPlanner.SmartPlanner.dto;

import lombok.Data;

import java.util.List;

@Data
public class DirectionsRequest {
    private double originLat;
    private double originLon;
    private double destLat;
    private double destLon;
    private String mode; // driving, walking, cycling
    private List<Waypoint> waypoints;

    @Data
    public static class Waypoint {
        private double lat;
        private double lon;
    }
}