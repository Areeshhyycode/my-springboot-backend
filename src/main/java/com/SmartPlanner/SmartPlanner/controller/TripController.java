package com.SmartPlanner.SmartPlanner.controller;

import com.SmartPlanner.SmartPlanner.dto.TripRequest;
import com.SmartPlanner.SmartPlanner.model.Trip;
import com.SmartPlanner.SmartPlanner.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TRIP CONTROLLER - User trip planning APIs
 *
 * All endpoints require authentication (USER or ADMIN)
 *
 * Base URL: /api/v1/trips
 */
@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TripController {

    private final TripService tripService;

    /**
     * CREATE TRIP - User trip plan kare
     *
     * URL: POST /api/v1/trips
     *
     * Headers:
     *   Authorization: Bearer <token>
     *
     * Request Body:
     * {
     *   "cityId": "676abc123...",
     *   "startDate": "2025-01-15",
     *   "durationDays": 3,
     *   "selectedActivities": [
     *     { "activityId": "676def456...", "quantity": 1 },
     *     { "activityId": "676ghi789...", "quantity": 2 }
     *   ]
     * }
     *
     * Response: Trip object with calculated total cost
     */
    @PostMapping
    public ResponseEntity<?> createTrip(
            @Valid @RequestBody TripRequest request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            // Using email as userId for simplicity
            Trip trip = tripService.createTrip(request, userEmail, userEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(trip);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * CALCULATE COST PREVIEW - Before booking, show cost breakdown
     *
     * URL: POST /api/v1/trips/preview
     *
     * Same request body as createTrip, but doesn't save
     */
    @PostMapping("/preview")
    public ResponseEntity<?> calculateCostPreview(@Valid @RequestBody TripRequest request) {
        try {
            TripService.CostPreview preview = tripService.calculateCostPreview(request);
            return ResponseEntity.ok(preview);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET MY TRIPS - User ke saare trips
     *
     * URL: GET /api/v1/trips
     */
    @GetMapping
    public ResponseEntity<List<Trip>> getMyTrips(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(tripService.getTripsByEmail(userEmail));
    }

    /**
     * GET TRIP BY ID
     *
     * URL: GET /api/v1/trips/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTripById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(tripService.getTripById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * CANCEL TRIP
     *
     * URL: DELETE /api/v1/trips/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelTrip(
            @PathVariable String id,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            Trip trip = tripService.cancelTrip(id, userEmail);
            return ResponseEntity.ok(trip);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * UPDATE TRIP STATUS (Admin can update any trip)
     *
     * URL: PATCH /api/v1/trips/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateTripStatus(
            @PathVariable String id,
            @RequestParam Trip.TripStatus status) {
        try {
            Trip trip = tripService.updateTripStatus(id, status);
            return ResponseEntity.ok(trip);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    record ErrorResponse(String message) {}
}
