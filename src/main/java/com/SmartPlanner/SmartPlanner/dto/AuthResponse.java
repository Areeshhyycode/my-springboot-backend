package com.SmartPlanner.SmartPlanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AUTH RESPONSE DTO
 *
 * Login/Register success hone ke baad ye response jaata hai
 * - token: JWT token (frontend isko store karega)
 * - message: Success message
 * - username: User ka naam
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String message;
    private String username;
    private String email;
    private String role;  // USER ya ADMIN
}