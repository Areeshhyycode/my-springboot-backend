package com.SmartPlanner.SmartPlanner.service;

import com.SmartPlanner.SmartPlanner.dto.AuthResponse;
import com.SmartPlanner.SmartPlanner.dto.LoginRequest;
import com.SmartPlanner.SmartPlanner.dto.RegisterRequest;
import com.SmartPlanner.SmartPlanner.model.Role;
import com.SmartPlanner.SmartPlanner.model.User;
import com.SmartPlanner.SmartPlanner.repository.UserRepository;
import com.SmartPlanner.SmartPlanner.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * AUTH SERVICE - Authentication ki business logic
 *
 * @Service - Spring ko batao ye service class hai
 * @RequiredArgsConstructor - Lombok: Constructor automatically generate (final fields ke liye)
 *
 * Flow:
 * Controller -> Service -> Repository -> Database
 */
@Service
@RequiredArgsConstructor  // Final fields ke liye constructor auto-generate
public class AuthService {

    private final UserRepository userRepository;  // Database operations
    private final PasswordEncoder passwordEncoder; // Password hashing
    private final JwtUtil jwtUtil;                // JWT token generation

    /**
     * REGISTER - New user create karo
     *
     * Steps:
     * 1. Check email already exists
     * 2. Check username already exists
     * 3. Password hash karo
     * 4. User save karo
     * 5. JWT token generate karo
     * 6. Response return karo
     */
    public AuthResponse register(RegisterRequest request) {
        // Step 1: Email check
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered!");
        }

        // Step 2: Username check
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken!");
        }

        // Step 3 & 4: User create aur save
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash password
        user.setRole(Role.USER);  // Default role USER
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);  // MongoDB mein save

        // Step 5: JWT token generate (with role)
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        // Step 6: Response return
        return new AuthResponse(
            token,
            "Registration successful!",
            user.getUsername(),
            user.getEmail(),
            user.getRole().name()
        );
    }

    /**
     * LOGIN - User authenticate karo
     *
     * Steps:
     * 1. Email se user dhundho
     * 2. Password match karo
     * 3. JWT token generate karo
     * 4. Response return karo
     */
    public AuthResponse login(LoginRequest request) {
        // Step 1: User dhundho
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Invalid email or password!"));

        // Step 2: Password check
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password!");
        }

        // Step 3: JWT token generate (with role)
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        // Step 4: Response return
        return new AuthResponse(
            token,
            "Login successful!",
            user.getUsername(),
            user.getEmail(),
            user.getRole().name()
        );
    }

    /**
     * REGISTER ADMIN - Sirf existing admin hi naya admin bana sakta hai
     */
    public AuthResponse registerAdmin(RegisterRequest request) {
        // Email check
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered!");
        }

        // Username check
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken!");
        }

        // Admin user create
        User admin = new User();
        admin.setUsername(request.getUsername());
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setRole(Role.ADMIN);  // ADMIN role
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());

        userRepository.save(admin);

        String token = jwtUtil.generateToken(admin.getEmail(), admin.getRole().name());

        return new AuthResponse(
            token,
            "Admin registration successful!",
            admin.getUsername(),
            admin.getEmail(),
            admin.getRole().name()
        );
    }
}