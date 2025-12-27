package com.SmartPlanner.SmartPlanner.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * COUNTRY MODEL - Top level entity
 *
 * Hierarchy:
 * Country (UAE)
 *   └── City (Dubai)
 *         └── Activity (Beach, Safari)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "countries")
public class Country {

    @Id
    private String id;

    private String name;           // Country name: "United Arab Emirates"
    private String code;           // Country code: "UAE" or "AE"
    private String imageUrl;       // Country flag/image
    private String description;    // Short description
    private Boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
