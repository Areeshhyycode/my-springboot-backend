package com.SmartPlanner.SmartPlanner.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final RestTemplate restTemplate;

    private static final String GEOCODING_API_URL =
            "https://geocoding-api.open-meteo.com/v1/search?name={cityName}&count=1&language=en&format=json";

    public GeoLocation getCoordinates(String cityName) {
        try {
            log.info("Fetching coordinates for city: {}", cityName);

            GeocodingResponse response = restTemplate.getForObject(
                    GEOCODING_API_URL,
                    GeocodingResponse.class,
                    cityName
            );

            if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
                throw new RuntimeException("City not found: " + cityName);
            }

            GeocodingResult result = response.getResults().get(0);

            log.info("Found: {} ({}) at ({}, {})",
                    result.getName(), result.getCountry(),
                    result.getLatitude(), result.getLongitude());

            return new GeoLocation(
                    result.getLatitude(),
                    result.getLongitude(),
                    result.getCountry(),
                    result.getName()
            );

        } catch (Exception e) {
            log.error("Geocoding failed for {}: {}", cityName, e.getMessage());
            throw new RuntimeException("Failed to fetch coordinates for: " + cityName);
        }
    }

    @Data
    public static class GeoLocation {
        private final Double latitude;
        private final Double longitude;
        private final String country;
        private final String displayName;
    }

    @Data
    public static class GeocodingResponse {
        private List<GeocodingResult> results;
    }

    @Data
    public static class GeocodingResult {
        private String name;
        private Double latitude;
        private Double longitude;
        private String country;
        private String timezone;
        private String admin1;
    }
}
