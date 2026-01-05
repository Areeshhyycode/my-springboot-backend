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

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserProfileController {

    private final UserProfileService profileService;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        String userId = authentication.getName();
        UserProfileResponse profile = profileService.getOrCreateProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PostMapping
    public ResponseEntity<UserProfileResponse> createProfile(
            @Valid @RequestBody UserProfileRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        UserProfileResponse profile = profileService.updateProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(profile);
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UserProfileRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        UserProfileResponse profile = profileService.updateProfile(userId, request);
        return ResponseEntity.ok(profile);
    }

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
                    .body(null);
        }
    }

    @PutMapping("/phone")
    public ResponseEntity<UserProfileResponse> updatePhoneNumber(
            @Valid @RequestBody PhoneUpdateRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        UserProfileResponse profile = profileService.updatePhoneNumber(userId, request);
        return ResponseEntity.ok(profile);
    }

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

    @DeleteMapping("/photo")
    public ResponseEntity<UserProfileResponse> deleteProfilePhoto(Authentication authentication) {
        String userId = authentication.getName();
        UserProfileResponse profile = profileService.deleteProfilePhoto(userId);
        return ResponseEntity.ok(profile);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
