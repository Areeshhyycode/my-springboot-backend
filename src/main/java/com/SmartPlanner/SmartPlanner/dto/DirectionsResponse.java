package com.SmartPlanner.SmartPlanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectionsResponse {
    private Coordinates origin;
    private Coordinates destination;
    private String distance;
    private String duration;
    private String mode;
    private String polyline;
    private List<RouteStep> steps;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteStep {
        private String instruction;
        private String distance;
        private String duration;
        private Coordinates startLocation;
        private Coordinates endLocation;
    }
}