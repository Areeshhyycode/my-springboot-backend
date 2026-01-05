package com.SmartPlanner.SmartPlanner.scheduler;

import com.SmartPlanner.SmartPlanner.model.Trip;
import com.SmartPlanner.SmartPlanner.model.TripNotification;
import com.SmartPlanner.SmartPlanner.model.TripNotification.NotificationType;
import com.SmartPlanner.SmartPlanner.repository.TripNotificationRepository;
import com.SmartPlanner.SmartPlanner.repository.TripRepository;
import com.SmartPlanner.SmartPlanner.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TripReminderScheduler {

    private final TripRepository tripRepository;
    private final TripNotificationRepository notificationRepository;
    private final EmailService emailService;

    // Run every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyReminders() {
        log.info("Starting daily trip reminder check...");

        LocalDate today = LocalDate.now();
        List<Trip> plannedTrips = tripRepository.findByStatus(Trip.TripStatus.PLANNED);

        for (Trip trip : plannedTrips) {
            if (trip.getStartDate() == null || trip.getUserEmail() == null) {
                continue;
            }

            long daysUntilTrip = ChronoUnit.DAYS.between(today, trip.getStartDate());

            // Send daily countdown reminders from 10 days to 1 day before trip
            if (daysUntilTrip >= 1 && daysUntilTrip <= 10) {
                NotificationType type = getNotificationTypeForDay(daysUntilTrip);
                if (type != null) {
                    sendReminder(trip, type, daysUntilTrip);
                }
            }
            // Trip starts today
            else if (daysUntilTrip == 0) {
                sendTripStartsTodayReminder(trip);
            }
        }

        // Check for completed trips
        checkAndCompleteTrips();

        log.info("Daily trip reminder check completed.");
    }

    // Get notification type based on days remaining
    private NotificationType getNotificationTypeForDay(long daysRemaining) {
        return switch ((int) daysRemaining) {
            case 10 -> NotificationType.REMINDER_10_DAYS;
            case 9 -> NotificationType.REMINDER_9_DAYS;
            case 8 -> NotificationType.REMINDER_8_DAYS;
            case 7 -> NotificationType.REMINDER_7_DAYS;
            case 6 -> NotificationType.REMINDER_6_DAYS;
            case 5 -> NotificationType.REMINDER_5_DAYS;
            case 4 -> NotificationType.REMINDER_4_DAYS;
            case 3 -> NotificationType.REMINDER_3_DAYS;
            case 2 -> NotificationType.REMINDER_2_DAYS;
            case 1 -> NotificationType.REMINDER_1_DAY;
            default -> null;
        };
    }

    // Send reminder based on type
    private void sendReminder(Trip trip, NotificationType type, long daysRemaining) {
        // Check if reminder already sent
        if (notificationRepository.existsByTripIdAndTypeAndEmailSentTrue(trip.getId(), type)) {
            log.debug("Reminder {} already sent for trip {}", type, trip.getId());
            return;
        }

        // Generate title and message based on days remaining
        String title = daysRemaining + " Days Until Your Trip!";
        String message = "Your trip to " + trip.getCityName() + " is in " + daysRemaining + " days.";

        // Special messages for specific days
        if (daysRemaining == 10) {
            title = "10 Days Until Your Trip!";
            message = "Your trip to " + trip.getCityName() + " is in 10 days. Start preparing!";
        } else if (daysRemaining == 7) {
            title = "One Week Until Your Trip!";
            message = "Your trip to " + trip.getCityName() + " is in one week. Time to prepare!";
        } else if (daysRemaining == 3) {
            title = "3 Days Until Your Trip!";
            message = "Your trip to " + trip.getCityName() + " is in 3 days. Final preparations!";
        } else if (daysRemaining == 1) {
            title = "Tomorrow is the Day!";
            message = "Your trip to " + trip.getCityName() + " starts tomorrow!";
        }

        // Send email using the generic daily countdown method
        emailService.sendDailyCountdownReminder(trip.getUserEmail(), trip.getCityName(),
                trip.getCountry(), trip.getStartDate(), daysRemaining);

        // Save notification
        TripNotification notification = new TripNotification(
                trip.getId(), trip.getUserId(), trip.getUserEmail(),
                type, title, message
        );
        notification.setEmailSent(true);
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);

        log.info("Sent {} reminder ({} days left) for trip {} to {}", type, daysRemaining, trip.getId(), trip.getUserEmail());
    }

    // Send trip starts today reminder
    private void sendTripStartsTodayReminder(Trip trip) {
        NotificationType type = NotificationType.TRIP_START_TODAY;

        if (notificationRepository.existsByTripIdAndTypeAndEmailSentTrue(trip.getId(), type)) {
            return;
        }

        emailService.sendTripStartsToday(trip.getUserEmail(), trip.getCityName(),
                trip.getCountry(), trip.getStartDate());

        TripNotification notification = new TripNotification(
                trip.getId(), trip.getUserId(), trip.getUserEmail(),
                type, "Your Trip Starts Today!",
                "Your trip to " + trip.getCityName() + " starts today! Have a great journey!"
        );
        notification.setEmailSent(true);
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);

        // Auto update status to ONGOING
        trip.setStatus(Trip.TripStatus.ONGOING);
        trip.setUpdatedAt(LocalDateTime.now());
        tripRepository.save(trip);

        log.info("Sent trip starts today reminder for trip {} and updated status to ONGOING", trip.getId());
    }

    // Check and auto-complete trips
    private void checkAndCompleteTrips() {
        LocalDate today = LocalDate.now();
        List<Trip> ongoingTrips = tripRepository.findByStatus(Trip.TripStatus.ONGOING);

        for (Trip trip : ongoingTrips) {
            if (trip.getEndDate() != null && today.isAfter(trip.getEndDate())) {
                // Trip has ended
                NotificationType type = NotificationType.TRIP_COMPLETED;

                if (!notificationRepository.existsByTripIdAndTypeAndEmailSentTrue(trip.getId(), type)) {
                    emailService.sendTripCompleted(trip.getUserEmail(), trip.getCityName(),
                            trip.getCountry(), trip.getStartDate(), trip.getEndDate());

                    TripNotification notification = new TripNotification(
                            trip.getId(), trip.getUserId(), trip.getUserEmail(),
                            type, "Trip Completed!",
                            "Your trip to " + trip.getCityName() + " has been completed. Welcome back!"
                    );
                    notification.setEmailSent(true);
                    notification.setSentAt(LocalDateTime.now());
                    notificationRepository.save(notification);

                    // Update trip status
                    trip.setStatus(Trip.TripStatus.COMPLETED);
                    trip.setUpdatedAt(LocalDateTime.now());
                    tripRepository.save(trip);

                    log.info("Auto-completed trip {} after end date", trip.getId());
                }
            }
        }
    }

    // Manual trigger for testing (can be called via API)
    public void triggerRemindersManually() {
        log.info("Manually triggering reminders...");
        sendDailyReminders();
    }
}
