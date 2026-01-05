package com.SmartPlanner.SmartPlanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeocodingResponse {
    private double latitude;
    private double longitude;
    private String displayName;
    private String type; // node, way, relation
    private String address;
    private String osmId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        private String road;
        private String suburb;
        private String city;
        private String state;
        private String postcode;
        private String country;
        private String countryCode;
    }
}