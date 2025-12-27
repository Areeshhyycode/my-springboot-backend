package com.SmartPlanner.SmartPlanner.config;

import com.SmartPlanner.SmartPlanner.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SECURITY CONFIGURATION
 *
 * Role-based access control:
 * - PUBLIC: Auth APIs, GET cities/categories/weather
 * - ADMIN: POST/PUT/DELETE cities/categories (under /api/v1/admin/*)
 * - USER: Trip planning APIs
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF disable (REST API ke liye)
            .csrf(csrf -> csrf.disable())

            // Session management - Stateless (JWT use kar rahe hain)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // URL permissions
            .authorizeHttpRequests(auth -> auth
                // ==================== PUBLIC URLs ====================
                // Auth APIs - Login/Register
                .requestMatchers("/api/v1/auth/**").permitAll()

                // GET requests - Countries, Cities, Categories, Weather (public read)
                .requestMatchers(HttpMethod.GET, "/api/v1/countries/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/cities/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/weather/**").permitAll()

                // Error page
                .requestMatchers("/error").permitAll()

                // ==================== ADMIN ONLY URLs ====================
                // Admin APIs - Countries, Cities & Categories CRUD
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // ==================== AUTHENTICATED (USER + ADMIN) ====================
                // Trip APIs - logged in users only
                .requestMatchers("/api/v1/trips/**").authenticated()
                .requestMatchers("/api/v1/user/**").authenticated()

                // Baaki saari URLs ke liye authentication chahiye
                .anyRequest().authenticated()
            )

            // JWT Filter add karo (UsernamePasswordAuthenticationFilter se pehle)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
