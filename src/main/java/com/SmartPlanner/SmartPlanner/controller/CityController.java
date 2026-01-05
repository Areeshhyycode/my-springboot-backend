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

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CityController {

    private final CityService cityService;

    @GetMapping("/api/v1/cities")
    public ResponseEntity<List<City>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    @GetMapping("/api/v1/cities/{id}")
    public ResponseEntity<?> getCityById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(cityService.getCityById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/api/v1/countries/{countryId}/cities")
    public ResponseEntity<?> getCitiesByCountry(@PathVariable String countryId) {
        try {
            return ResponseEntity.ok(cityService.getCitiesByCountry(countryId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/api/v1/admin/cities")
    public ResponseEntity<?> addCity(@Valid @RequestBody CityRequest request) {
        try {
            City city = cityService.addCity(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(city);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/api/v1/admin/cities/{id}")
    public ResponseEntity<?> updateCity(@PathVariable String id, @Valid @RequestBody CityRequest request) {
        try {
            City city = cityService.updateCity(id, request);
            return ResponseEntity.ok(city);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/api/v1/admin/cities/{id}")
    public ResponseEntity<?> deleteCity(@PathVariable String id) {
        try {
            cityService.deleteCity(id);
            return ResponseEntity.ok(new SuccessResponse("City deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PatchMapping("/api/v1/admin/cities/{id}/toggle")
    public ResponseEntity<?> toggleCityStatus(@PathVariable String id) {
        try {
            City city = cityService.toggleCityStatus(id);
            return ResponseEntity.ok(city);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/api/v1/admin/cities/{id}/refresh-weather")
    public ResponseEntity<?> refreshCityWeather(@PathVariable String id) {
        try {
            City city = cityService.refreshWeather(id);
            return ResponseEntity.ok(city);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/api/v1/admin/cities/refresh-all-weather")
    public ResponseEntity<List<City>> refreshAllWeather() {
        return ResponseEntity.ok(cityService.refreshAllWeather());
    }

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
