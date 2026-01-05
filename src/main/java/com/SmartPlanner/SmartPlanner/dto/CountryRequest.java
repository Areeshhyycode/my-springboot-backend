package com.SmartPlanner.SmartPlanner.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountryRequest {

    @NotBlank(message = "Country name is required")
    private String name;

    private String code;

    private String imageUrl;
    private String description;
}
