package com.SmartPlanner.SmartPlanner.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT UTILITY CLASS
 *
 * JWT (JSON Web Token) = User ko authenticate karne ka tarika
 *
 * Flow:
 * 1. User login karta hai (email + password)
 * 2. Server JWT token generate karta hai
 * 3. Frontend token ko localStorage mein store karta hai
 * 4. Har request ke saath token header mein bhejta hai
 * 5. Server token verify karta hai
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")          // application.properties se secret key
    private String secretKey;

    @Value("${jwt.expiration}")      // Token expiry time (milliseconds)
    private long expirationTime;

    // Secret key ko SecretKey object mein convert karo
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * JWT Token Generate karo (with role)
     *
     * @param email - User ki email
     * @param role - User ka role (ADMIN/USER)
     * @return JWT token string
     */
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)                                          // Token mein email store
                .claim("role", role)                                     // Role bhi store karo
                .issuedAt(new Date())                                    // Token kab bana
                .expiration(new Date(System.currentTimeMillis() + expirationTime))  // Kab expire hoga
                .signWith(getSigningKey())                               // Secret key se sign karo
                .compact();                                              // Token string return
    }

    /**
     * Token se email nikalo
     */
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Token se role nikalo
     */
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    /**
     * Check karo token valid hai ya nahi
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Token se claims (data) nikalo
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}