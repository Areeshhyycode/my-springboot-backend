package com.SmartPlanner.SmartPlanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripCountdownResponse {

    private String tripId;
    private String cityName;
    private String country;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;

    private Countdown countdown;
    private String message;
    private Boolean isTripStarted;
    private Boolean isTripEnded;
    private Boolean requiresCompletionConfirmation;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Countdown {
        private long days;
        private long hours;
        private long minutes;
        private long seconds;
        private long totalSeconds;
    }
}
