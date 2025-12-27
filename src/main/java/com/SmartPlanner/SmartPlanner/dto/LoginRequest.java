package com.SmartPlanner.SmartPlanner.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * LOGIN REQUEST DTO
 *
 * Login ke liye sirf email aur password chahiye
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide valid email")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}