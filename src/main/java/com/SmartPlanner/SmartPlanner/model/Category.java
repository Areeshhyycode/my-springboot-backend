package com.SmartPlanner.SmartPlanner.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

/**
 * CATEGORY MODEL - Trip categories (Fishing, Boating, Sea Food, etc.)
 *
 * Har category kisi city se linked hogi
 * User categories select karke trip plan karega
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "categories")
public class Category {

    @Id
    private String id;

    private String name;           // Category name: "Fishing", "Boating", "Sea Food"
    private String description;    // Description
    private String cityId;         // Kis city mein hai ye category
    private BigDecimal pricePerHour;   // Price per hour
    private BigDecimal pricePerDay;    // Price per day
    private String imageUrl;       // Category image
    private Boolean isActive = true;  // Active/Inactive category
}
