package com.SmartPlanner.SmartPlanner.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TRIP MODEL - User ka trip plan store karta hai
 *
 * User select karega:
 * - City
 * - Duration (days)
 * - Activities
 *
 * Backend calculate karega:
 * - Total cost based on selected activities
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trips")
public class Trip {

    @Id
    private String id;

    // User info
    private String userId;
    private String userEmail;

    // City info
    private String cityId;
    private String cityName;
    private String country;

    // Trip dates
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer durationDays;

    // Selected activities
    private List<SelectedActivity> selectedActivities;

    // Cost calculation
    private BigDecimal totalCost;
    private String currency = "USD";

    // Weather at time of booking
    private City.CityWeather weatherSnapshot;

    // Trip status
    private TripStatus status = TripStatus.PLANNED;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Selected Activity - User ne jo activity select ki
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectedActivity {
        private String activityId;
        private String activityName;
        private String durationType;    // "HOURS" or "DAYS"
        private Integer durationValue;  // e.g., 3 hours or 2 days
        private BigDecimal unitPrice;   // price per hour or per day
        private Integer quantity;       // kitni baar (default 1)
        private BigDecimal subtotal;    // unitPrice * durationValue * quantity
    }

    /**
     * Trip Status
     */
    public enum TripStatus {
        PLANNED,      // Trip planned but not confirmed
        CONFIRMED,    // Trip confirmed
        IN_PROGRESS,  // Currently on trip
        COMPLETED,    // Trip completed
        CANCELLED     // Trip cancelled
    }
}
