package com.SmartPlanner.SmartPlanner.controller;

import com.SmartPlanner.SmartPlanner.dto.*;
import com.SmartPlanner.SmartPlanner.security.JwtUtil;
import com.SmartPlanner.SmartPlanner.service.UserProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TEST CLASS FOR UserProfileController
 * Tests all user profile endpoints
 */
@WebMvcTest(controllers = UserProfileController.class)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Use MockitoBean instead of MockBean
    @MockitoBean
    private UserProfileService profileService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private UserProfileResponse profileResponse;
    private UserProfileRequest profileRequest;

    @BeforeEach
    void setUp() {
        // Setup profile response
        profileResponse = new UserProfileResponse();
        profileResponse.setUserId("user123");
        profileResponse.setUsername("testuser");
        profileResponse.setEmail("test@example.com");
        profileResponse.setPhoneNumber("+923001234567");
        profileResponse.setBio("Test bio");
        profileResponse.setDateOfBirth(LocalDate.of(1990, 1, 1));
        profileResponse.setAddress("123 Test Street");

        // Setup profile request
        profileRequest = new UserProfileRequest();
        profileRequest.setPhoneNumber("+923001234567");
        profileRequest.setBio("Test bio");
        profileRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        profileRequest.setAddress("123 Test Street");
        profileRequest.setLanguage("en");
    }

    @Test
    @DisplayName("Get profile - Success")
    void testGetProfile_Success() throws Exception {
        // Mock the service call
        when(profileService.getOrCreateProfile(eq("user123")))
                .thenReturn(profileResponse);

        // Make the request with authenticated user
        mockMvc.perform(get("/api/v1/profile")
                        .with(user("user123").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("Create profile - Success")
    void testCreateProfile_Success() throws Exception {
        when(profileService.updateProfile(eq("user123"), any(UserProfileRequest.class)))
                .thenReturn(profileResponse);

        mockMvc.perform(post("/api/v1/profile")
                        .with(user("user123").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.phoneNumber").value("+923001234567"));
    }

    @Test
    @DisplayName("Update profile - Success")
    void testUpdateProfile_Success() throws Exception {
        profileRequest.setBio("Updated bio");
        profileResponse.setBio("Updated bio");

        when(profileService.updateProfile(eq("user123"), any(UserProfileRequest.class)))
                .thenReturn(profileResponse);

        mockMvc.perform(put("/api/v1/profile")
                        .with(user("user123").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("Updated bio"));
    }

    @Test
    @DisplayName("Update username - Success")
    void testUpdateUsername_Success() throws Exception {
        UsernameUpdateRequest request = new UsernameUpdateRequest();
        request.setUsername("newusername");

        profileResponse.setUsername("newusername");

        when(profileService.updateUsername(eq("user123"), any(UsernameUpdateRequest.class)))
                .thenReturn(profileResponse);

        mockMvc.perform(put("/api/v1/profile/username")
                        .with(user("user123").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newusername"));
    }

    @Test
    @DisplayName("Update username - Username already taken")
    void testUpdateUsername_AlreadyTaken() throws Exception {
        UsernameUpdateRequest request = new UsernameUpdateRequest();
        request.setUsername("takenusername");

        when(profileService.updateUsername(eq("user123"), any(UsernameUpdateRequest.class)))
                .thenThrow(new RuntimeException("Username already taken"));

        mockMvc.perform(put("/api/v1/profile/username")
                        .with(user("user123").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Update phone number - Success")
    void testUpdatePhoneNumber_Success() throws Exception {
        PhoneUpdateRequest request = new PhoneUpdateRequest();
        request.setPhoneNumber("+923009876543");

        profileResponse.setPhoneNumber("+923009876543");

        when(profileService.updatePhoneNumber(eq("user123"), any(PhoneUpdateRequest.class)))
                .thenReturn(profileResponse);

        mockMvc.perform(put("/api/v1/profile/phone")
                        .with(user("user123").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value("+923009876543"));
    }

    @Test
    @DisplayName("Upload profile photo - Success")
    void testUploadProfilePhoto_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        profileResponse.setProfilePhotoUrl("/uploads/profile-photos/user123/profile.jpg");

        when(profileService.uploadProfilePhoto(eq("user123"), any()))
                .thenReturn(profileResponse);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/profile/photo")
                        .file(file)
                        .with(user("user123").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePhotoUrl").exists());
    }

    @Test
    @DisplayName("Upload profile photo - Invalid file type")
    void testUploadProfilePhoto_InvalidFileType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        when(profileService.uploadProfilePhoto(eq("user123"), any()))
                .thenThrow(new IllegalArgumentException("Invalid file type"));

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/profile/photo")
                        .file(file)
                        .with(user("user123").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Upload profile photo - IO Exception")
    void testUploadProfilePhoto_IOException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        when(profileService.uploadProfilePhoto(eq("user123"), any()))
                .thenThrow(new IOException("Failed to save file"));

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/profile/photo")
                        .file(file)
                        .with(user("user123").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Delete profile photo - Success")
    void testDeleteProfilePhoto_Success() throws Exception {
        profileResponse.setProfilePhotoUrl(null);

        when(profileService.deleteProfilePhoto(eq("user123")))
                .thenReturn(profileResponse);

        mockMvc.perform(delete("/api/v1/profile/photo")
                        .with(user("user123").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePhotoUrl").doesNotExist());
    }

    @Test
    @DisplayName("Get profile - Unauthenticated request")
    void testGetProfile_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Update profile - Invalid data")
    void testUpdateProfile_InvalidData() throws Exception {
        UserProfileRequest invalidRequest = new UserProfileRequest();

        when(profileService.updateProfile(eq("user123"), any(UserProfileRequest.class)))
                .thenThrow(new RuntimeException("Invalid profile data"));

        mockMvc.perform(put("/api/v1/profile")
                        .with(user("user123").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}