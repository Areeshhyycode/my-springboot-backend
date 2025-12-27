package com.SmartPlanner.SmartPlanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    /**
     * USER PROFILE RESPONSE DTO
     * Frontend ko complete profile data bhejne ke liye
     */

    private String id;
    private String userId;

    // User basic info (from User model)
    private String username;
    private String email;
    private String role;  // Account Type: USER or ADMIN

    // Profile info
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String address;
    private String profilePhotoUrl;
    private String language;
    private String bio;

}
