package com.SmartPlanner.SmartPlanner.service;

import com.SmartPlanner.SmartPlanner.dto.*;
import com.SmartPlanner.SmartPlanner.model.User;
import com.SmartPlanner.SmartPlanner.model.UserProfile;
import com.SmartPlanner.SmartPlanner.repository.UserProfileRepository;
import com.SmartPlanner.SmartPlanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * USER PROFILE SERVICE
 * Business logic for user profile operations
 */
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService; // File upload

    /**
     * Create or Get User Profile
     * @param email - User email from JWT token
     */
    @Transactional
    public UserProfileResponse getOrCreateProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile(user.getId());
                    return profileRepository.save(newProfile);
                });

        return mapToResponse(profile, user);
    }

    /**
     * Update User Profile
     * @param email - User email from JWT token
     */
    @Transactional
    public UserProfileResponse updateProfile(String email, UserProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = profileRepository.findByUserId(user.getId())
                .orElse(new UserProfile(user.getId()));

        // Update profile fields
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        if (request.getProfilePhotoUrl() != null) {
            profile.setProfilePhotoUrl(request.getProfilePhotoUrl());
        }
        if (request.getLanguage() != null) {
            profile.setLanguage(request.getLanguage());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }

        profile.setUpdatedAt(LocalDateTime.now());
        UserProfile savedProfile = profileRepository.save(profile);

        return mapToResponse(savedProfile, user);
    }

    /**
     * Update Username (from User model)
     * @param email - User email from JWT token
     */
    @Transactional
    public UserProfileResponse updateUsername(String email, UsernameUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if username already exists
        Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
        if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
            throw new RuntimeException("Username already taken");
        }

        user.setUsername(request.getUsername());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        UserProfile profile = profileRepository.findByUserId(user.getId())
                .orElse(new UserProfile(user.getId()));

        return mapToResponse(profile, user);
    }

    /**
     * Update Phone Number
     * @param email - User email from JWT token
     */
    @Transactional
    public UserProfileResponse updatePhoneNumber(String email, PhoneUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = profileRepository.findByUserId(user.getId())
                .orElse(new UserProfile(user.getId()));

        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setUpdatedAt(LocalDateTime.now());
        UserProfile savedProfile = profileRepository.save(profile);

        return mapToResponse(savedProfile, user);
    }

    /**
     * Upload Profile Photo
     * @param email - User email from JWT token
     */
    @Transactional
    public UserProfileResponse uploadProfilePhoto(String email, MultipartFile file) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = profileRepository.findByUserId(user.getId())
                .orElse(new UserProfile(user.getId()));

        // Delete old photo if exists
        if (profile.getProfilePhotoUrl() != null) {
            fileStorageService.deleteFile(profile.getProfilePhotoUrl());
        }

        // Upload new photo
        String photoUrl = fileStorageService.uploadFile(file, "profile-photos/" + user.getId());
        profile.setProfilePhotoUrl(photoUrl);
        profile.setUpdatedAt(LocalDateTime.now());
        UserProfile savedProfile = profileRepository.save(profile);

        return mapToResponse(savedProfile, user);
    }

    /**
     * Delete Profile Photo
     * @param email - User email from JWT token
     */
    @Transactional
    public UserProfileResponse deleteProfilePhoto(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (profile.getProfilePhotoUrl() != null) {
            fileStorageService.deleteFile(profile.getProfilePhotoUrl());
            profile.setProfilePhotoUrl(null);
            profile.setUpdatedAt(LocalDateTime.now());
            profileRepository.save(profile);
        }

        return mapToResponse(profile, user);
    }

    /**
     * Map to Response DTO
     */
    private UserProfileResponse mapToResponse(UserProfile profile, User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUserId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());  // Account Type: USER or ADMIN
        response.setPhoneNumber(profile.getPhoneNumber());
        response.setDateOfBirth(profile.getDateOfBirth());
        response.setAddress(profile.getAddress());
        response.setProfilePhotoUrl(profile.getProfilePhotoUrl());
        response.setLanguage(profile.getLanguage());
        response.setBio(profile.getBio());
        return response;
    }
}