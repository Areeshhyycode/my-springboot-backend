package com.SmartPlanner.SmartPlanner.service;

import com.SmartPlanner.SmartPlanner.dto.NotificationResponse;
import com.SmartPlanner.SmartPlanner.dto.TripCountdownResponse;
import com.SmartPlanner.SmartPlanner.model.Trip;
import com.SmartPlanner.SmartPlanner.model.TripNotification;
import com.SmartPlanner.SmartPlanner.model.TripNotification.NotificationType;
import com.SmartPlanner.SmartPlanner.repository.TripNotificationRepository;
import com.SmartPlanner.SmartPlanner.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Slf4j
public class TripNotificationService {

    private final TripRepository tripRepository;
    private final TripNotificationRepository notificationRepository;
    private final EmailService emailService;

    // Get countdown for a specific trip
    public TripCountdownResponse getTripCountdown(String tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found with id: " + tripId));

        return buildCountdownResponse(trip);
    }

    // Get all upcoming trips with countdown for a user
    public List<TripCountdownResponse> getUserUpcomingTripsCountdown(String userEmail) {
        try {
            log.info("getUserUpcomingTripsCountdown called for email: {}", userEmail);

            if (userEmail == null || userEmail.isBlank()) {
                log.warn("getUserUpcomingTripsCountdown called with null/blank email");
                return List.of();
            }

            // Saare trips lo aur manually filter karo
            log.info("Fetching all trips from repository...");
            List<Trip> allTrips = tripRepository.findAll();
            log.info("Found {} total trips", allTrips.size());

            // Filter by user email and PLANNED status (with null safety)
            List<Trip> userPlannedTrips = allTrips.stream()
                    .filter(trip -> userEmail.equals(trip.getUserEmail()))
                    .filter(trip -> trip.getStatus() != null && trip.getStatus() == Trip.TripStatus.PLANNED)
                    .filter(trip -> trip.getStartDate() != null)
                    .filter(trip -> !trip.getStartDate().isBefore(LocalDate.now()))
                    .collect(Collectors.toList());

            log.info("Found {} upcoming planned trips for user", userPlannedTrips.size());

            List<TripCountdownResponse> result = userPlannedTrips.stream()
                    .map(trip -> {
                        try {
                            return buildCountdownResponse(trip);
                        } catch (Exception e) {
                            log.error("Error building countdown for trip {}: {}", trip.getId(), e.getMessage(), e);
                            return null;
                        }
                    })
                    .filter(response -> response != null)
                    .sorted(Comparator.comparingLong(a -> a.getCountdown().getTotalSeconds()))
                    .collect(Collectors.toList());

            log.info("Returning {} countdown responses", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error in getUserUpcomingTripsCountdown: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Build countdown response
    private TripCountdownResponse buildCountdownResponse(Trip trip) {
        TripCountdownResponse response = new TripCountdownResponse();
        response.setTripId(trip.getId());
        response.setCityName(trip.getCityName());
        response.setCountry(trip.getCountry());
        response.setStartDate(trip.getStartDate());
        response.setEndDate(trip.getEndDate());
        response.setStatus(trip.getStatus() != null ? trip.getStatus().name() : "PLANNED");

        LocalDate today = LocalDate.now();
        LocalDate startDate = trip.getStartDate();
        LocalDate endDate = trip.getEndDate();

        // Initialize countdown with zeros
        TripCountdownResponse.Countdown countdown = new TripCountdownResponse.Countdown(0, 0, 0, 0, 0);
        response.setCountdown(countdown);

        // Check if trip has started
        response.setIsTripStarted(!today.isBefore(startDate));
        response.setIsTripEnded(endDate != null && today.isAfter(endDate));

        // Check if trip ended but not marked as completed - needs user confirmation
        boolean tripEndedButNotCompleted = (endDate != null && today.isAfter(endDate))
                && (trip.getStatus() == null || trip.getStatus() == Trip.TripStatus.PLANNED);
        response.setRequiresCompletionConfirmation(tripEndedButNotCompleted);

        // Calculate countdown only if trip hasn't started
        if (today.isBefore(startDate)) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tripStart = LocalDateTime.of(startDate, LocalTime.of(12, 0)); // Assume trip starts at noon

            if (tripStart.isAfter(now)) {
                Duration duration = Duration.between(now, tripStart);

                countdown.setTotalSeconds(duration.getSeconds());
                countdown.setDays(duration.toDays());
                countdown.setHours(duration.toHoursPart());
                countdown.setMinutes(duration.toMinutesPart());
                countdown.setSeconds(duration.toSecondsPart());

                long days = duration.toDays();
                if (days > 0) {
                    response.setMessage("Your trip to " + trip.getCityName() + " starts in " + days + " days");
                } else {
                    long hours = duration.toHours();
                    if (hours > 0) {
                        response.setMessage("Your trip to " + trip.getCityName() + " starts in " + hours + " hours");
                    } else {
                        long minutes = duration.toMinutes();
                        response.setMessage("Your trip to " + trip.getCityName() + " starts in " + minutes + " minutes");
                    }
                }
            } else {
                response.setMessage("Your trip to " + trip.getCityName() + " is starting soon!");
            }
        } else if (endDate != null && today.isAfter(endDate)) {
            if (trip.getStatus() == null || trip.getStatus() == Trip.TripStatus.PLANNED) {
                response.setMessage("Your trip to " + trip.getCityName() + " has ended. Would you like to mark it as completed?");
            } else {
                response.setMessage("Your trip to " + trip.getCityName() + " has ended");
            }
        } else {
            response.setMessage("Your trip to " + trip.getCityName() + " is ongoing!");
        }

        return response;
    }

    // Update trip status
    public Trip updateTripStatus(String tripId, Trip.TripStatus newStatus, String userEmail) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // Check if user owns this trip
        if (!userEmail.equals(trip.getUserEmail())) {
            throw new RuntimeException("You are not authorized to update this trip");
        }

        trip.setStatus(newStatus);
        trip.setUpdatedAt(LocalDateTime.now());
        Trip updatedTrip = tripRepository.save(trip);

        // Create notification
        createStatusChangeNotification(trip, newStatus);

        // Send email
        sendStatusChangeEmail(trip, newStatus);

        return updatedTrip;
    }

    // Cancel trip
    public Trip cancelTrip(String tripId, String userEmail) {
        return updateTripStatus(tripId, Trip.TripStatus.CANCELLED, userEmail);
    }

    // Create notification for status change
    private void createStatusChangeNotification(Trip trip, Trip.TripStatus newStatus) {
        NotificationType type;
        String title;
        String message;

        switch (newStatus) {
            case ONGOING:
                type = NotificationType.TRIP_ONGOING;
                title = "Trip Started!";
                message = "Your trip to " + trip.getCityName() + " is now ongoing. Enjoy!";
                break;
            case COMPLETED:
                type = NotificationType.TRIP_COMPLETED;
                title = "Trip Completed!";
                message = "Your trip to " + trip.getCityName() + " has been marked as completed.";
                break;
            case CANCELLED:
                type = NotificationType.TRIP_CANCELLED;
                title = "Trip Cancelled";
                message = "Your trip to " + trip.getCityName() + " has been cancelled.";
                break;
            default:
                return;
        }

        TripNotification notification = new TripNotification(
                trip.getId(), trip.getUserId(), trip.getUserEmail(),
                type, title, message
        );
        notificationRepository.save(notification);
    }

    // Send email for status change
    private void sendStatusChangeEmail(Trip trip, Trip.TripStatus newStatus) {
        switch (newStatus) {
            case COMPLETED:
                emailService.sendTripCompleted(trip.getUserEmail(), trip.getCityName(),
                        trip.getCountry(), trip.getStartDate(), trip.getEndDate());
                break;
            case CANCELLED:
                emailService.sendTripCancelled(trip.getUserEmail(), trip.getCityName(),
                        trip.getCountry());
                break;
            default:
                break;
        }
    }

    // Get user notifications
    public List<NotificationResponse> getUserNotifications(String userEmail) {
        List<TripNotification> notifications = notificationRepository
                .findByUserEmailOrderByCreatedAtDesc(userEmail);

        return notifications.stream()
                .map(this::mapToNotificationResponse)
                .collect(Collectors.toList());
    }

    // Get unread notifications
    public List<NotificationResponse> getUnreadNotifications(String userEmail) {
        List<TripNotification> notifications = notificationRepository
                .findByUserEmailAndIsReadFalseOrderByCreatedAtDesc(userEmail);

        return notifications.stream()
                .map(this::mapToNotificationResponse)
                .collect(Collectors.toList());
    }

    // Get unread count
    public long getUnreadCount(String userEmail) {
        return notificationRepository.countByUserEmailAndIsReadFalse(userEmail);
    }

    // Mark notification as read
    public void markAsRead(String notificationId) {
        TripNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    // Mark all as read
    public void markAllAsRead(String userEmail) {
        List<TripNotification> notifications = notificationRepository
                .findByUserEmailAndIsReadFalseOrderByCreatedAtDesc(userEmail);

        notifications.forEach(n -> {
            n.setIsRead(true);
            n.setReadAt(LocalDateTime.now());
        });

        notificationRepository.saveAll(notifications);
    }

    // Map to response DTO
    private NotificationResponse mapToNotificationResponse(TripNotification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTripId(notification.getTripId());
        response.setType(notification.getType().name());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setIsRead(notification.getIsRead());
        response.setCreatedAt(notification.getCreatedAt());

        // Get trip details
        tripRepository.findById(notification.getTripId()).ifPresent(trip -> {
            response.setCityName(trip.getCityName());
            response.setCountry(trip.getCountry());
        });

        return response;
    }

    // Test email - for debugging
    public void sendTestEmail(String toEmail, String cityName) {
        log.info("Sending test email to: {}", toEmail);
        emailService.sendDailyCountdownReminder(toEmail, cityName, "Test Country",
                LocalDate.now().plusDays(5), 5);
        log.info("Test email method completed for: {}", toEmail);
    }
}