package com.SmartPlanner.SmartPlanner.controller;

import com.SmartPlanner.SmartPlanner.dto.*;
import com.SmartPlanner.SmartPlanner.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * USER PROFILE CONTROLLER
 * REST API endpoints for user profile management
 *
 * Base URL: /api/v1/profile
 */
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserProfileController {

    private final UserProfileService profileService;

    /**
     * GET /api/profile
     * Get current user's profile
     * Authentication required
     */
    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        String userId = authentication.getName(); // JWT se userId extract
        UserProfileResponse profile = profileService.getOrCreateProfile(userId);
        return ResponseEntity.ok(profile);
    }

    /**
     * POST /api/v1/profile
     * Create user profile
     * Request Body: UserProfileRequest (JSON)
     */
    @PostMapping
    public ResponseEntity<UserProfileResponse> createProfile(
            @Valid @RequestBody UserProfileRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        UserProfileResponse profile = profileService.updateProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(profile);
    }

    /**
     * PUT /api/profile
     * Update user profile
     * Request Body: UserProfileRequest (JSON)
     */
    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UserProfileRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        UserProfileResponse profile = profileService.updateProfile(userId, request);
        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /api/profile/username
     * Update username
     * Request Body: { "username": "newusername" }
     */
    @PutMapping("/username")
    public ResponseEntity<UserProfileResponse> updateUsername(
            @Valid @RequestBody UsernameUpdateRequest request,
            Authentication authentication) {
        try {
            String userId = authentication.getName();
            UserProfileResponse profile = profileService.updateUsername(userId, request);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null); // Username already taken
        }
    }

    /**
     * PUT /api/profile/phone
     * Update phone number
     * Request Body: { "phoneNumber": "+923001234567" }
     */
    @PutMapping("/phone")
    public ResponseEntity<UserProfileResponse> updatePhoneNumber(
            @Valid @RequestBody PhoneUpdateRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        UserProfileResponse profile = profileService.updatePhoneNumber(userId, request);
        return ResponseEntity.ok(profile);
    }

    /**
     * POST /api/profile/photo
     * Upload profile photo
     * Content-Type: multipart/form-data
     * Form field: "file"
     */
    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfilePhoto(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            String userId = authentication.getName();
            UserProfileResponse profile = profileService.uploadProfilePhoto(userId, file);
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload file");
        }
    }

    /**
     * DELETE /api/profile/photo
     * Delete profile photo
     */
    @DeleteMapping("/photo")
    public ResponseEntity<UserProfileResponse> deleteProfilePhoto(Authentication authentication) {
        String userId = authentication.getName();
        UserProfileResponse profile = profileService.deleteProfilePhoto(userId);
        return ResponseEntity.ok(profile);
    }

    /**
     * Exception Handler
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}