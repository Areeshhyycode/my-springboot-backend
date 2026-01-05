package com.SmartPlanner.SmartPlanner.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripStatusUpdateRequest {

    @NotBlank(message = "Status is required")
    private String status;  // PLANNED, ONGOING, COMPLETED, CANCELLED

    private String userResponse;  // Optional: "Yes", "Ready", etc.
}
