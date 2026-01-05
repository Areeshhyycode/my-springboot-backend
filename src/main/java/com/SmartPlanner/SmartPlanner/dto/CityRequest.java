package com.SmartPlanner.SmartPlanner.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityRequest {

    @NotBlank(message = "Country ID is required")
    private String countryId;

    @NotBlank(message = "City name is required")
    private String name;

    private String imageUrl;
    private String description;
}
