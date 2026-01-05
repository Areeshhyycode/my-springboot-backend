package com.SmartPlanner.SmartPlanner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripRequest {

    @NotBlank(message = "City ID is required")
    private String cityId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    private Integer durationDays;

    @NotEmpty(message = "At least one activity must be selected")
    private List<ActivitySelection> selectedActivities;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivitySelection {
        @NotBlank(message = "Activity ID is required")
        private String activityId;

        @NotNull(message = "Duration type is required")
        private DurationType durationType;

        @NotNull(message = "Duration value is required")
        @Positive(message = "Duration value must be positive")
        private Integer durationValue;

        @Positive(message = "Quantity must be positive")
        private Integer quantity = 1;
    }

    public enum DurationType {
        HOURS,
        DAYS
    }
}
