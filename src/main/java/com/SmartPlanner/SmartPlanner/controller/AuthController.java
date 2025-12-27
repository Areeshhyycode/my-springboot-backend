package com.SmartPlanner.SmartPlanner.controller;

import com.SmartPlanner.SmartPlanner.dto.AuthResponse;
import com.SmartPlanner.SmartPlanner.dto.LoginRequest;
import com.SmartPlanner.SmartPlanner.dto.RegisterRequest;
import com.SmartPlanner.SmartPlanner.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")  // React frontend se requests allow
public class AuthController {

    private final AuthService authService;

    /**
     * REGISTER API
     *
     * URL: POST http://localhost:8080/api/v1/auth/register
     *
     * Request Body (JSON):
     * {
     *   "username": "john",
     *   "email": "john@example.com",
     *   "password": "password123"
     * }
     *
     * Response:
     * {
     *   "token": "eyJhbGc...",
     *   "message": "Registration successful!",
     *   "username": "john",
     *   "email": "john@example.com"
     * }
     *
     * @Valid - Request body ko validate karo (DTO mein jo annotations hain)
     * @RequestBody - JSON ko Java object mein convert karo
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * LOGIN API
     *
     * URL: POST http://localhost:8080/api/v1/auth/login
     *
     * Request Body (JSON):
     * {
     *   "email": "john@example.com",
     *   "password": "password123"
     * }
     *
     * Response:
     * {
     *   "token": "eyJhbGc...",
     *   "message": "Login successful!",
     *   "username": "john",
     *   "email": "john@example.com"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * REGISTER ADMIN API (Protected - only existing admin can create new admin)
     *
     * URL: POST http://localhost:8080/api/v1/auth/register-admin
     *
     * Headers:
     *   Authorization: Bearer <admin_token>
     *
     * Request Body (JSON):
     * {
     *   "username": "admin2",
     *   "email": "admin2@example.com",
     *   "password": "admin123"
     * }
     *
     * Response:
     * {
     *   "token": "eyJhbGc...",
     *   "message": "Admin registration successful!",
     *   "username": "admin2",
     *   "email": "admin2@example.com",
     *   "role": "ADMIN"
     * }
     */
    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.registerAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * CREATE FIRST ADMIN (Use only once to create first admin)
     *
     * URL: POST http://localhost:8080/api/v1/auth/setup-admin
     *
     * Note: Production mein yeh endpoint disable kar dena
     */
    @PostMapping("/setup-admin")
    public ResponseEntity<?> setupFirstAdmin(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.registerAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * TEST API - Check if auth endpoints work
     *
     * URL: GET http://localhost:8080/api/v1/auth/test
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth API is working!");
    }

    // Error response ke liye inner class
    record ErrorResponse(String message) {}
}