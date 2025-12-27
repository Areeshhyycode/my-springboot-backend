package com.SmartPlanner.SmartPlanner.controller;

import com.SmartPlanner.SmartPlanner.dto.CityRequest;
import com.SmartPlanner.SmartPlanner.model.City;
import com.SmartPlanner.SmartPlanner.service.CityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CITY CONTROLLER - Cities under Countries
 *
 * Hierarchy:
 * Country (UAE)
 *   └── City (Dubai) ← Managed here
 *         └── Activity (Beach, Safari)
 *
 * PUBLIC APIs:
 *   - GET /api/v1/cities - All cities
 *   - GET /api/v1/cities/{id} - City by ID
 *   - GET /api/v1/countries/{countryId}/cities - Cities in a country
 *
 * ADMIN ONLY APIs:
 *   - POST /api/v1/admin/cities - Add city (with countryId)
 *   - PUT /api/v1/admin/cities/{id} - Update city
 *   - DELETE /api/v1/admin/cities/{id} - Delete city
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CityController {

    private final CityService cityService;

    // ==================== PUBLIC APIs ====================

    /**
     * GET ALL CITIES
     * URL: GET /api/v1/cities
     */
    @GetMapping("/api/v1/cities")
    public ResponseEntity<List<City>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    /**
     * GET CITY BY ID
     * URL: GET /api/v1/cities/{id}
     */
    @GetMapping("/api/v1/cities/{id}")
    public ResponseEntity<?> getCityById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(cityService.getCityById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET CITIES BY COUNTRY
     * URL: GET /api/v1/countries/{countryId}/cities
     *
     * User clicks on country card → Shows all cities in that country
     */
    @GetMapping("/api/v1/countries/{countryId}/cities")
    public ResponseEntity<?> getCitiesByCountry(@PathVariable String countryId) {
        try {
            return ResponseEntity.ok(cityService.getCitiesByCountry(countryId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ==================== ADMIN ONLY APIs ====================

    /**
     * ADD CITY under a Country (Admin Only)
     *
     * URL: POST /api/v1/admin/cities
     *
     * Request Body:
     * {
     *   "countryId": "676abc...",
     *   "name": "Dubai"
     * }
     *
     * Backend auto-fetches:
     * - Latitude & Longitude
     * - Weather
     */
    @PostMapping("/api/v1/admin/cities")
    public ResponseEntity<?> addCity(@Valid @RequestBody CityRequest request) {
        try {
            City city = cityService.addCity(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(city);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * UPDATE CITY (Admin Only)
     * URL: PUT /api/v1/admin/cities/{id}
     */
    @PutMapping("/api/v1/admin/cities/{id}")
    public ResponseEntity<?> updateCity(@PathVariable String id, @Valid @RequestBody CityRequest request) {
        try {
            City city = cityService.updateCity(id, request);
            return ResponseEntity.ok(city);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * DELETE CITY (Admin Only)
     * URL: DELETE /api/v1/admin/cities/{id}
     */
    @DeleteMapping("/api/v1/admin/cities/{id}")
    public ResponseEntity<?> deleteCity(@PathVariable String id) {
        try {
            cityService.deleteCity(id);
            return ResponseEntity.ok(new SuccessResponse("City deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * TOGGLE CITY STATUS (Admin Only)
     * URL: PATCH /api/v1/admin/cities/{id}/toggle
     */
    @PatchMapping("/api/v1/admin/cities/{id}/toggle")
    public ResponseEntity<?> toggleCityStatus(@PathVariable String id) {
        try {
            City city = cityService.toggleCityStatus(id);
            return ResponseEntity.ok(city);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * REFRESH WEATHER for single city (Admin Only)
     * URL: POST /api/v1/admin/cities/{id}/refresh-weather
     */
    @PostMapping("/api/v1/admin/cities/{id}/refresh-weather")
    public ResponseEntity<?> refreshCityWeather(@PathVariable String id) {
        try {
            City city = cityService.refreshWeather(id);
            return ResponseEntity.ok(city);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * REFRESH WEATHER for all cities (Admin Only)
     * URL: POST /api/v1/admin/cities/refresh-all-weather
     */
    @PostMapping("/api/v1/admin/cities/refresh-all-weather")
    public ResponseEntity<List<City>> refreshAllWeather() {
        return ResponseEntity.ok(cityService.refreshAllWeather());
    }

    /**
     * SEED SAMPLE CITIES for a country (Admin Only)
     * URL: POST /api/v1/admin/countries/{countryId}/cities/seed
     */
    @PostMapping("/api/v1/admin/countries/{countryId}/cities/seed")
    public ResponseEntity<?> seedCities(@PathVariable String countryId) {
        try {
            return ResponseEntity.ok(cityService.addSampleCities(countryId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    record ErrorResponse(String message) {}
    record SuccessResponse(String message) {}
}
