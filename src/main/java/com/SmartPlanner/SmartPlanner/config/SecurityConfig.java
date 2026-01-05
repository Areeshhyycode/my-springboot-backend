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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        // ✅ PUBLIC
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        // Weather APIs
                        .requestMatchers("/api/weather/**").permitAll()
                        .requestMatchers("/api/v1/weather/**").permitAll()

                        // Maps APIs
                        .requestMatchers("/api/v1/maps/**").permitAll()

                        // Public GET APIs
                        .requestMatchers(HttpMethod.GET, "/api/v1/countries/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/cities/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()

                        // ✅ COUNTDOWN & NOTIFICATION APIs (ADD THESE)
                        .requestMatchers("/api/v1/countdown/**").authenticated()
                        .requestMatchers("/api/v1/notifications/**").authenticated()

                        // Trip status update endpoints
                        .requestMatchers(HttpMethod.PUT, "/api/v1/trips/*/start").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/trips/*/complete").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/trips/*/cancel").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/trips/*/update-status").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/trips/*/status").authenticated()

                        // ✅ ADMIN
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/trips/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/admin/trigger-reminders").hasRole("ADMIN")

                        // ✅ TRIPS (ALL METHODS EXPLICIT)
                        .requestMatchers(HttpMethod.GET, "/api/v1/trips/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/trips/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/trips/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/trips/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/trips/**").authenticated()

                        // ✅ USER
                        .requestMatchers("/api/v1/user/**").authenticated()
                        .requestMatchers("/api/v1/profile/**").authenticated()

                        // ✅ FALLBACK
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://localhost:3000",
                "http://127.0.0.1:5173",
                "http://127.0.0.1:3000",
                "http://localhost:8080",
                "http://127.0.0.1:8080"
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));

        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "Accept", "X-Requested-With",
                "Cache-Control", "Origin", "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        configuration.setExposedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "Content-Disposition",
                "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"
        ));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}