package com.SmartPlanner.SmartPlanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistanceResponse {
    private Coordinates point1;
    private Coordinates point2;
    private double distanceKm;
    private double distanceMiles;
    private String estimatedDuration; // for driving
}