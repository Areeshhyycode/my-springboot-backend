package com.SmartPlanner.SmartPlanner.controller;

import com.SmartPlanner.SmartPlanner.dto.NotificationResponse;
import com.SmartPlanner.SmartPlanner.dto.TripCountdownResponse;
import com.SmartPlanner.SmartPlanner.dto.TripStatusUpdateRequest;
import com.SmartPlanner.SmartPlanner.model.Trip;
import com.SmartPlanner.SmartPlanner.repository.UserRepository;
import com.SmartPlanner.SmartPlanner.scheduler.TripReminderScheduler;
import com.SmartPlanner.SmartPlanner.service.TripNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trip Countdown & Notifications", description = "Trip countdown timer and notification management")
public class TripNotificationController {

    private final TripNotificationService notificationService;
    private final TripReminderScheduler reminderScheduler;

    // ==================== COUNTDOWN ENDPOINTS ====================

    @GetMapping("/countdown/trip/{tripId}")
    @Operation(summary = "Get countdown for a specific trip")
    public ResponseEntity<TripCountdownResponse> getTripCountdown(@PathVariable String tripId) {
        TripCountdownResponse countdown = notificationService.getTripCountdown(tripId);
        return ResponseEntity.ok(countdown);
    }

    @GetMapping("/countdown/my-trips")
    @Operation(summary = "Get countdown for all user's upcoming trips")
    public ResponseEntity<?> getMyTripsCountdown(Authentication authentication) {
        try {
            log.info("getMyTripsCountdown endpoint called");
            log.info("Authentication object: {}", authentication);

            if (authentication == null) {
                log.warn("Authentication is null");
                return ResponseEntity.status(401).body(Map.of("error", "Authentication is null"));
            }

            log.info("Authentication name: {}", authentication.getName());
            log.info("Authentication principal: {}", authentication.getPrincipal());

            if (authentication.getName() == null) {
                log.warn("Authentication name is null");
                return ResponseEntity.status(401).body(Map.of("error", "Authentication name is null"));
            }

            String email = authentication.getName();
            log.info("Fetching countdown for email: {}", email);

            // Direct email use karein kyunki aapke Trip model mein userId field actually email hi hai
            List<TripCountdownResponse> countdowns = notificationService.getUserUpcomingTripsCountdown(email);
            log.info("Found {} countdowns", countdowns.size());

            return ResponseEntity.ok(countdowns);
        } catch (Exception e) {
            log.error("Error in getMyTripsCountdown: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage(), "type", e.getClass().getName()));
        }
    }

    // ==================== TRIP STATUS ENDPOINTS ====================

    @PutMapping({"/trips/{tripId}/update-status", "/trips/{tripId}/status"})
    @Operation(summary = "Update trip status (PLANNED, ONGOING, IN_PROGRESS, COMPLETED, CANCELLED)")
    public ResponseEntity<Map<String, Object>> updateTripStatus(
            @PathVariable String tripId,
            @RequestBody(required = false) TripStatusUpdateRequest request,
            @RequestParam(required = false) String status,
            Authentication authentication) {

        try {
            log.info("updateTripStatus called for tripId: {}", tripId);
            log.info("Request body: {}", request);
            log.info("Status param: {}", status);

            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
            }

            // Get status from request body or query param
            String statusValue = null;
            if (request != null && request.getStatus() != null) {
                statusValue = request.getStatus();
            } else if (status != null) {
                statusValue = status;
            }

            if (statusValue == null || statusValue.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Status is required",
                        "usage", "Send JSON body {\"status\": \"COMPLETED\"} or use query param ?status=COMPLETED"
                ));
            }

            String email = authentication.getName();
            Trip.TripStatus newStatus = Trip.TripStatus.valueOf(statusValue.toUpperCase());

            Trip updatedTrip = notificationService.updateTripStatus(tripId, newStatus, email);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Trip status updated successfully");
            response.put("tripId", updatedTrip.getId());
            response.put("newStatus", updatedTrip.getStatus().name());
            response.put("cityName", updatedTrip.getCityName());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid status value: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid status value",
                    "validValues", "PLANNED, ONGOING, IN_PROGRESS, COMPLETED, CANCELLED"
            ));
        } catch (Exception e) {
            log.error("Error updating trip status: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/trips/{tripId}/cancel")
    @Operation(summary = "Cancel a trip")
    public ResponseEntity<Map<String, Object>> cancelTrip(
            @PathVariable String tripId,
            Authentication authentication) {

        String email = authentication.getName();
        Trip cancelledTrip = notificationService.cancelTrip(tripId, email);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Trip cancelled successfully");
        response.put("tripId", cancelledTrip.getId());
        response.put("status", "CANCELLED");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/trips/{tripId}/start")
    @Operation(summary = "Mark trip as started/ongoing")
    public ResponseEntity<Map<String, Object>> startTrip(
            @PathVariable String tripId,
            Authentication authentication) {

        String email = authentication.getName();
        Trip trip = notificationService.updateTripStatus(tripId, Trip.TripStatus.ONGOING, email);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Trip marked as ongoing!");
        response.put("tripId", trip.getId());
        response.put("status", "ONGOING");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/trips/{tripId}/complete")
    @Operation(summary = "Mark trip as completed")
    public ResponseEntity<Map<String, Object>> completeTrip(
            @PathVariable String tripId,
            Authentication authentication) {

        String email = authentication.getName();
        Trip trip = notificationService.updateTripStatus(tripId, Trip.TripStatus.COMPLETED, email);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Trip completed! Welcome back!");
        response.put("tripId", trip.getId());
        response.put("status", "COMPLETED");

        return ResponseEntity.ok(response);
    }

    // ==================== NOTIFICATION ENDPOINTS ====================

    @GetMapping("/notifications")
    @Operation(summary = "Get all notifications for logged-in user")
    public ResponseEntity<List<NotificationResponse>> getNotifications(Authentication authentication) {
        String email = authentication.getName();
        List<NotificationResponse> notifications = notificationService.getUserNotifications(email);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/notifications/unread")
    @Operation(summary = "Get unread notifications")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(Authentication authentication) {
        String email = authentication.getName();
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(email);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/notifications/unread/count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        String email = authentication.getName();
        long count = notificationService.getUnreadCount(email);

        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/notifications/{notificationId}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable String notificationId) {
        notificationService.markAsRead(notificationId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification marked as read");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/notifications/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Map<String, String>> markAllAsRead(Authentication authentication) {
        String email = authentication.getName();
        notificationService.markAllAsRead(email);

        Map<String, String> response = new HashMap<>();
        response.put("message", "All notifications marked as read");

        return ResponseEntity.ok(response);
    }

    // ==================== ADMIN/TEST ENDPOINTS ====================

    @PostMapping("/admin/trigger-reminders")
    @Operation(summary = "Manually trigger reminder scheduler (Admin/Testing)")
    public ResponseEntity<Map<String, String>> triggerReminders() {
        reminderScheduler.triggerRemindersManually();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Reminder scheduler triggered successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/test-email")
    @Operation(summary = "Test email sending (Admin/Testing)")
    public ResponseEntity<Map<String, String>> testEmail(
            @RequestParam String email,
            @RequestParam(defaultValue = "Test City") String cityName) {
        try {
            log.info("Testing email to: {}", email);
            notificationService.sendTestEmail(email, cityName);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Test email sent to " + email);
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Test email failed: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to send email: " + e.getMessage());
            response.put("status", "error");

            return ResponseEntity.status(500).body(response);
        }
    }
}