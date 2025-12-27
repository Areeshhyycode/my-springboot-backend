package com.SmartPlanner.SmartPlanner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * USER PROFILE REQUEST DTO
 * Frontend se profile data receive karne ke liye
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Size(max = 200, message = "Address too long")
    private String address;

    @Size(max = 500, message = "Profile photo URL too long")
    private String profilePhotoUrl;

    @Size(min = 2, max = 5, message = "Language code must be 2-5 characters")
    private String language;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
}

