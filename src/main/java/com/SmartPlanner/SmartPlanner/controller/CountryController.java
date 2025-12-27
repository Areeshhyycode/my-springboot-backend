package com.SmartPlanner.SmartPlanner.controller;

import com.SmartPlanner.SmartPlanner.dto.CountryRequest;
import com.SmartPlanner.SmartPlanner.dto.FullCountryRequest;
import com.SmartPlanner.SmartPlanner.dto.FullCountryResponse;
import com.SmartPlanner.SmartPlanner.model.Country;
import com.SmartPlanner.SmartPlanner.service.CountryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * COUNTRY CONTROLLER
 *
 * PUBLIC APIs:
 *   - GET /api/v1/countries/full - All countries with cities & activities (FRONTEND)
 *   - GET /api/v1/countries/{id}/full - Single country with cities & activities
 *
 * ADMIN APIs:
 *   - POST /api/v1/admin/countries/full - Add country + cities + activities
 *   - POST /api/v1/admin/countries - Add country only
 *   - PUT/DELETE for CRUD
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CountryController {

    private final CountryService countryService;

    // ==================== PUBLIC APIs (For Frontend) ====================

    /**
     * GET ALL DATA - Countries + Cities (with weather) + Activities
     *
     * URL: GET /api/v1/countries/full
     *
     * Use this for FRONTEND to show everything!
     */
    @GetMapping("/api/v1/countries/full")
    public ResponseEntity<List<FullCountryResponse>> getAllCountriesWithCitiesAndActivities() {
        return ResponseEntity.ok(countryService.getAllCountriesWithCitiesAndActivities());
    }

    /**
     * GET SINGLE COUNTRY with Cities and Activities
     *
     * URL: GET /api/v1/countries/{id}/full
     */
    @GetMapping("/api/v1/countries/{id}/full")
    public ResponseEntity<?> getCountryWithCitiesAndActivities(@PathVariable String id) {
        try {
            return ResponseEntity.ok(countryService.getCountryWithCitiesAndActivities(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET ALL COUNTRIES (basic - without cities)
     * URL: GET /api/v1/countries
     */
    @GetMapping("/api/v1/countries")
    public ResponseEntity<List<Country>> getAllCountries() {
        return ResponseEntity.ok(countryService.getActiveCountries());
    }

    /**
     * GET COUNTRY BY ID
     * URL: GET /api/v1/countries/{id}
     */
    @GetMapping("/api/v1/countries/{id}")
    public ResponseEntity<?> getCountryById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(countryService.getCountryById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ==================== ADMIN ONLY APIs ====================

    /**
     * ADD FULL COUNTRY - Country + Cities + Activities in ONE API
     *
     * URL: POST /api/v1/admin/countries/full
     *
     * Request Body:
     * {
     *   "name": "United Arab Emirates",
     *   "code": "UAE",
     *   "cities": [
     *     {
     *       "name": "Dubai",
     *       "activities": [
     *         {"name": "Beach", "price": 50, "duration": 3},
     *         {"name": "Desert Safari", "price": 100, "duration": 5}
     *       ]
     *     },
     *     {
     *       "name": "Abu Dhabi",
     *       "activities": [
     *         {"name": "Mosque Tour", "price": 30, "duration": 2}
     *       ]
     *     }
     *   ]
     * }
     */
    @PostMapping("/api/v1/admin/countries/full")
    public ResponseEntity<?> addFullCountry(@Valid @RequestBody FullCountryRequest request) {
        try {
            FullCountryResponse response = countryService.addFullCountry(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * ADD COUNTRY (simple - without cities)
     * URL: POST /api/v1/admin/countries
     */
    @PostMapping("/api/v1/admin/countries")
    public ResponseEntity<?> addCountry(@Valid @RequestBody CountryRequest request) {
        try {
            Country country = countryService.addCountry(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(country);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * UPDATE COUNTRY
     * URL: PUT /api/v1/admin/countries/{id}
     */
    @PutMapping("/api/v1/admin/countries/{id}")
    public ResponseEntity<?> updateCountry(@PathVariable String id, @Valid @RequestBody CountryRequest request) {
        try {
            Country country = countryService.updateCountry(id, request);
            return ResponseEntity.ok(country);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * DELETE COUNTRY (deletes all cities and activities too)
     * URL: DELETE /api/v1/admin/countries/{id}
     */
    @DeleteMapping("/api/v1/admin/countries/{id}")
    public ResponseEntity<?> deleteCountry(@PathVariable String id) {
        try {
            countryService.deleteCountry(id);
            return ResponseEntity.ok(new SuccessResponse("Country and all its data deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * TOGGLE COUNTRY STATUS
     * URL: PATCH /api/v1/admin/countries/{id}/toggle
     */
    @PatchMapping("/api/v1/admin/countries/{id}/toggle")
    public ResponseEntity<?> toggleCountryStatus(@PathVariable String id) {
        try {
            Country country = countryService.toggleCountryStatus(id);
            return ResponseEntity.ok(country);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    record ErrorResponse(String message) {}
    record SuccessResponse(String message) {}
}
