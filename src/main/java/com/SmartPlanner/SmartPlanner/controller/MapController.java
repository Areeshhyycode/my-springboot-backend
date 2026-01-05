package com.SmartPlanner.SmartPlanner.controller;

import com.SmartPlanner.SmartPlanner.dto.DirectionsRequest;
import com.SmartPlanner.SmartPlanner.dto.DirectionsResponse;
import com.SmartPlanner.SmartPlanner.dto.DistanceResponse;
import com.SmartPlanner.SmartPlanner.dto.GeocodingResponse;
import com.SmartPlanner.SmartPlanner.dto.MapCityResponse;
import com.SmartPlanner.SmartPlanner.dto.NearbyPlacesResponse;
import com.SmartPlanner.SmartPlanner.service.MapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/maps")
@RequiredArgsConstructor
@Tag(name = "Maps Integration", description = "Free maps integration using OpenStreetMap")
public class MapController {

    private final MapService mapService;

    @GetMapping("/city/{cityId}")
    @Operation(summary = "Get city map data with activity markers")
    public ResponseEntity<MapCityResponse> getCityMapData(@PathVariable String cityId) {
        // TODO: Integrate with CityRepository and CategoryRepository
        // For now, returning sample data structure
        MapCityResponse response = new MapCityResponse();
        response.setCityId(cityId);
        response.setCityName("Sample City");
        response.setCenter(new MapCityResponse.Coordinates(24.8607, 67.0011));
        response.setBoundingBox(new MapCityResponse.BoundingBox(25.0, 24.7, 67.3, 66.7));
        response.setMarkers(new ArrayList<>());

        // Add sample marker
        MapCityResponse.MapMarker marker = new MapCityResponse.MapMarker();
        marker.setId("cat1");
        marker.setName("Clifton Beach");
        marker.setType("activity");
        marker.setLatitude(24.7937);
        marker.setLongitude(66.9629);
        marker.setDescription("Famous beach");
        response.getMarkers().add(marker);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/directions")
    @Operation(summary = "Get route between two points")
    public ResponseEntity<DirectionsResponse> getDirections(
            @RequestParam double originLat,
            @RequestParam double originLon,
            @RequestParam double destLat,
            @RequestParam double destLon,
            @RequestParam(defaultValue = "driving") String mode) {

        DirectionsResponse response = mapService.getDirections(
                originLat, originLon, destLat, destLon, mode);

        return response != null ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().build();
    }

    @PostMapping("/route")
    @Operation(summary = "Get multi-stop route for trip")
    public ResponseEntity<DirectionsResponse> getMultiStopRoute(
            @RequestBody DirectionsRequest request) {

        List<double[]> coordinates = new ArrayList<>();
        coordinates.add(new double[]{request.getOriginLat(), request.getOriginLon()});

        if (request.getWaypoints() != null) {
            for (DirectionsRequest.Waypoint wp : request.getWaypoints()) {
                coordinates.add(new double[]{wp.getLat(), wp.getLon()});
            }
        }

        coordinates.add(new double[]{request.getDestLat(), request.getDestLon()});

        DirectionsResponse response = mapService.getMultiStopRoute(
                coordinates, request.getMode());

        return response != null ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().build();
    }

    @GetMapping("/nearby")
    @Operation(summary = "Get nearby POIs")
    public ResponseEntity<NearbyPlacesResponse> getNearbyPlaces(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "5000") int radius,
            @RequestParam(defaultValue = "tourism") String type) {

        NearbyPlacesResponse response = mapService.getNearbyPlaces(
                lat, lon, radius, type);

        return response != null ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().build();
    }

    @GetMapping("/distance")
    @Operation(summary = "Calculate distance between two coordinates")
    public ResponseEntity<DistanceResponse> calculateDistance(
            @RequestParam double lat1,
            @RequestParam double lon1,
            @RequestParam double lat2,
            @RequestParam double lon2) {

        DistanceResponse response = mapService.calculateDistance(
                lat1, lon1, lat2, lon2);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/geocode")
    @Operation(summary = "Convert address to coordinates")
    public ResponseEntity<GeocodingResponse> geocodeAddress(
            @RequestParam String address) {

        GeocodingResponse response = mapService.geocodeAddress(address);

        return response != null ?
                ResponseEntity.ok(response) :
                ResponseEntity.notFound().build();
    }

    @GetMapping("/reverse-geocode")
    @Operation(summary = "Convert coordinates to address")
    public ResponseEntity<GeocodingResponse> reverseGeocode(
            @RequestParam double lat,
            @RequestParam double lon) {

        GeocodingResponse response = mapService.reverseGeocode(lat, lon);

        return response != null ?
                ResponseEntity.ok(response) :
                ResponseEntity.notFound().build();
    }

    @GetMapping("/trip/{tripId}/route")
    @Operation(summary = "Get complete trip route with all activities")
    public ResponseEntity<DirectionsResponse> getTripRoute(@PathVariable String tripId) {
        DirectionsResponse response = mapService.getTripRoute(tripId);
        return response != null ?
                ResponseEntity.ok(response) :
                ResponseEntity.notFound().build();
    }
}