package com.SmartPlanner.SmartPlanner.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trip_notifications")
public class TripNotification {

    @Id
    private String id;

    private String tripId;
    private String userId;
    private String userEmail;

    private NotificationType type; // ✅ Yeh hai "type", "status" nahi
    private String title;
    private String message;

    private Boolean isRead = false;
    private Boolean emailSent = false;

    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;

    private LocalDateTime createdAt;

    public enum NotificationType {
        // Daily countdown reminders (10 days to 1 day)
        REMINDER_10_DAYS,
        REMINDER_9_DAYS,
        REMINDER_8_DAYS,
        REMINDER_7_DAYS,
        REMINDER_6_DAYS,
        REMINDER_5_DAYS,
        REMINDER_4_DAYS,
        REMINDER_3_DAYS,
        REMINDER_2_DAYS,
        REMINDER_1_DAY,

        // Trip status notifications
        TRIP_START_TODAY,      // Trip starts today
        TRIP_ONGOING,          // Trip is ongoing
        TRIP_COMPLETED,        // Trip completed
        TRIP_CANCELLED         // Trip cancelled
    }

    // Constructor for quick creation
    public TripNotification(String tripId, String userId, String userEmail,
                            NotificationType type, String title, String message) {
        this.tripId = tripId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.type = type;
        this.title = title;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
}
