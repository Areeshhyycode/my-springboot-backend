package com.SmartPlanner.SmartPlanner.dto;

import lombok.Data;

@Data
public class NearbyPlacesRequest {
    private double lat;
    private double lon;
    private int radius = 5000; // in meters
    private String type; // restaurant, hotel, atm, hospital, tourism, etc.
    private int limit = 50;
}