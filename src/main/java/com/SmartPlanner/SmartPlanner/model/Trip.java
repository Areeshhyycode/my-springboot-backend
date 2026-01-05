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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trips")
public class Trip {

    @Id
    private String id;

    private String userId;
    private String userEmail;

    private String cityId;
    private String cityName;
    private String country;

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer durationDays;

    private List<SelectedActivity> selectedActivities;

    private BigDecimal totalCost;
    private String currency = "USD";

    private City.CityWeather weatherSnapshot;

    private TripStatus status = TripStatus.PLANNED;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectedActivity {
        private String activityId;
        private String name;
        private String durationType;
        private Integer durationValue;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal subtotal;
        private Double latitude;
        private Double longitude;
    }

    public enum TripStatus {
        PLANNED,
        CONFIRMED,    // Legacy status
        ONGOING,
        IN_PROGRESS,  // Legacy status - same as ONGOING
        COMPLETED,
        CANCELLED
    }
}
