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

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CountryController {

    private final CountryService countryService;

    @GetMapping("/api/v1/countries/full")
    public ResponseEntity<List<FullCountryResponse>> getAllCountriesWithCitiesAndActivities() {
        return ResponseEntity.ok(countryService.getAllCountriesWithCitiesAndActivities());
    }

    @GetMapping("/api/v1/countries/{id}/full")
    public ResponseEntity<?> getCountryWithCitiesAndActivities(@PathVariable String id) {
        try {
            return ResponseEntity.ok(countryService.getCountryWithCitiesAndActivities(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/api/v1/countries")
    public ResponseEntity<List<Country>> getAllCountries() {
        return ResponseEntity.ok(countryService.getActiveCountries());
    }

    @GetMapping("/api/v1/countries/{id}")
    public ResponseEntity<?> getCountryById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(countryService.getCountryById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/api/v1/admin/countries/full")
    public ResponseEntity<?> addFullCountry(@Valid @RequestBody FullCountryRequest request) {
        try {
            FullCountryResponse response = countryService.addFullCountry(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/api/v1/admin/countries")
    public ResponseEntity<?> addCountry(@Valid @RequestBody CountryRequest request) {
        try {
            Country country = countryService.addCountry(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(country);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/api/v1/admin/countries/{id}")
    public ResponseEntity<?> updateCountry(@PathVariable String id, @Valid @RequestBody CountryRequest request) {
        try {
            Country country = countryService.updateCountry(id, request);
            return ResponseEntity.ok(country);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/api/v1/admin/countries/{id}")
    public ResponseEntity<?> deleteCountry(@PathVariable String id) {
        try {
            countryService.deleteCountry(id);
            return ResponseEntity.ok(new SuccessResponse("Country and all its data deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

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
