package com.SmartPlanner.SmartPlanner.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * USER PROFILE MODEL - Extended user information
 * User ke detailed profile information ko store karta hai
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_profiles")
public class UserProfile {

    @Id
    private String id;

    private String userId;  // User collection se link (foreign key)

    // Basic Information
    private String phoneNumber;
    private LocalDate dateOfBirth;

    // Location Information
    private String address;

    // Profile Details
    private String profilePhotoUrl;
    private String language;         // Preferred language (e.g., "en", "ur", "es")
    private String bio;              // About me/Bio section

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor with userId for initialization
    public UserProfile(String userId) {
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}