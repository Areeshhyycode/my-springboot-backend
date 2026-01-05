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

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public UserProfileResponse getOrCreateProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile(user.getId(), user.getUsername(), user.getEmail());
                    newProfile.setFullName(user.getFullName());
                    return profileRepository.save(newProfile);
                });

        // Sync user data if changed
        if (!user.getUsername().equals(profile.getUsername()) ||
                !user.getEmail().equals(profile.getEmail()) ||
                (user.getFullName() != null && !user.getFullName().equals(profile.getFullName()))) {

            profile.setUsername(user.getUsername());
            profile.setEmail(user.getEmail());
            profile.setFullName(user.getFullName());
            profile.setUpdatedAt(LocalDateTime.now());
            profileRepository.save(profile);
        }

        return mapToResponse(profile, user);
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, UserProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = profileRepository.findByUserId(user.getId())
                .orElse(new UserProfile(user.getId(), user.getUsername(), user.getEmail()));

        // Update User entity if username changed
        boolean userUpdated = false;
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Username already taken");
            }
            user.setUsername(request.getUsername());
            userUpdated = true;
        }

        // Note: Email update should be separate endpoint for security
        if (userUpdated) {
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        // Update profile fields
        profile.setUsername(user.getUsername());
        profile.setFullName(request.getFullName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());
        profile.setCountry(request.getCountry());
        profile.setCity(request.getCity());
        profile.setBio(request.getBio());
        profile.setPreferredTravelTypes(request.getPreferredTravelTypes());
        profile.setUpdatedAt(LocalDateTime.now());

        UserProfile savedProfile = profileRepository.save(profile);

        return mapToResponse(savedProfile, user);
    }

    @Transactional
    public UserProfileResponse updateUsername(String email, UsernameUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if username is already taken by another user
        Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
        if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
            throw new RuntimeException("Username already taken");
        }

        // Update User entity
        user.setUsername(request.getUsername());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Update Profile entity
        UserProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile(user.getId(), request.getUsername(), user.getEmail());
                    newProfile.setFullName(user.getFullName());
                    return newProfile;
                });

        profile.setUsername(request.getUsername());
        profile.setUpdatedAt(LocalDateTime.now());
        profileRepository.save(profile);

        return mapToResponse(profile, user);
    }

    @Transactional
    public UserProfileResponse updatePhoneNumber(String email, PhoneUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile(user.getId(), user.getUsername(), user.getEmail());
                    newProfile.setFullName(user.getFullName());
                    return newProfile;
                });

        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setUpdatedAt(LocalDateTime.now());
        UserProfile savedProfile = profileRepository.save(profile);

        return mapToResponse(savedProfile, user);
    }

    @Transactional
    public UserProfileResponse uploadProfilePhoto(String email, MultipartFile file) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile(user.getId(), user.getUsername(), user.getEmail());
                    newProfile.setFullName(user.getFullName());
                    return newProfile;
                });

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

    private UserProfileResponse mapToResponse(UserProfile profile, User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUserId());
        response.setUsername(profile.getUsername());
        response.setEmail(profile.getEmail());
        response.setFullName(profile.getFullName());
        response.setPhoneNumber(profile.getPhoneNumber());
        response.setDateOfBirth(profile.getDateOfBirth());
        response.setGender(profile.getGender());
        response.setCountry(profile.getCountry());
        response.setCity(profile.getCity());
        response.setProfilePhotoUrl(profile.getProfilePhotoUrl());
        response.setBio(profile.getBio());
        response.setPreferredTravelTypes(profile.getPreferredTravelTypes());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());

        return response;
    }
}