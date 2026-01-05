package com.SmartPlanner.SmartPlanner.controller;

import com.SmartPlanner.SmartPlanner.dto.TripRequest;
import com.SmartPlanner.SmartPlanner.model.Trip;
import com.SmartPlanner.SmartPlanner.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/trips", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TripController {

    private final TripService tripService;

    // ✅ CREATE
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createTrip(
            @Valid @RequestBody TripRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        Trip trip = tripService.createTrip(request, userEmail, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(trip);
    }

    // ✅ PREVIEW
    @PostMapping(value = "/preview", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> calculateCostPreview(
            @Valid @RequestBody TripRequest request) {

        TripService.CostPreview preview =
                tripService.calculateCostPreview(request);
        return ResponseEntity.ok(preview);
    }

    // ✅ GET ALL (User's trips)
    @GetMapping
    public ResponseEntity<List<Trip>> getMyTrips(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(tripService.getTripsByEmail(userEmail));
    }

    // ✅ ADMIN: GET ALL TRIPS
    @GetMapping("/admin/all")
    public ResponseEntity<List<Trip>> getAllTrips() {
        return ResponseEntity.ok(tripService.getAllTrips());
    }

    // ✅ GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getTripById(@PathVariable String id) {
        return ResponseEntity.ok(tripService.getTripById(id));
    }

    // ✅ 🔥 FIXED PUT (THIS WAS THE ISSUE)
    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> editTrip(
            @PathVariable String id,
            @Valid @RequestBody TripRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        Trip trip = tripService.editTrip(id, request, userEmail);
        return ResponseEntity.ok(trip);
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelTrip(
            @PathVariable String id,
            Authentication authentication) {

        String userEmail = authentication.getName();
        Trip trip = tripService.cancelTrip(id, userEmail);
        return ResponseEntity.ok(trip);
    }

    // ✅ PATCH STATUS
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateTripStatus(
            @PathVariable String id,
            @RequestParam Trip.TripStatus status) {

        Trip trip = tripService.updateTripStatus(id, status);
        return ResponseEntity.ok(trip);
    }

    record ErrorResponse(String message) {}
}
