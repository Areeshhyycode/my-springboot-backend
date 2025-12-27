package com.SmartPlanner.SmartPlanner.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT AUTHENTICATION FILTER
 *
 * Har request pe yeh filter check karta hai:
 * 1. Authorization header mein token hai?
 * 2. Token valid hai?
 * 3. Token se user details nikalo
 * 4. SecurityContext mein set karo
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // Step 1: Token extract karo
            String token = extractToken(request);

            // Step 2: Token validate karo
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {

                // Step 3: Token se email aur role nikalo
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);

                log.debug("Authenticated user: {}, role: {}", email, role);

                // Step 4: Authority create karo (ROLE_ prefix required by Spring Security)
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

                // Step 5: Authentication token create karo
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                Collections.singletonList(authority)
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Step 6: SecurityContext mein set karo
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            log.error("JWT Authentication failed: {}", e.getMessage());
        }

        // Next filter ko call karo
        filterChain.doFilter(request, response);
    }

    /**
     * Authorization header se token extract karo
     *
     * Header format: "Bearer eyJhbGc..."
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer " ke baad ka part
        }
        return null;
    }
}
