package com.marketplace.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT Token Provider
 * Handles JWT token generation and validation
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret:your-secret-key-change-this-in-production-with-at-least-256-bits}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    /**
     * Generate JWT token for user
     * @param userId the user ID
     * @param username the username
     * @param role the user role (CUSTOMER, SERVICE_PROVIDER, ADMIN)
     * @return JWT token string
     */
    public String generateToken(Long userId, String username, String role) {
        log.info("Generating JWT token for user: {}", username);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String token = Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        log.info("JWT token generated successfully for user: {}", username);
        return token;
    }

    /**
     * Get user ID from JWT token
     * @param token the JWT token
     * @return user ID
     */
    public Long getUserIdFromToken(String token) {
        log.debug("Extracting user ID from JWT token");

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("userId", Long.class);
    }

    /**
     * Get username from JWT token
     * @param token the JWT token
     * @return username
     */
    public String getUsernameFromToken(String token) {
        log.debug("Extracting username from JWT token");

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Get role from JWT token
     * @param token the JWT token
     * @return role string (e.g. "ADMIN", "CUSTOMER", "SERVICE_PROVIDER")
     */
    public String getRoleFromToken(String token) {
        log.debug("Extracting role from JWT token");

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("role", String.class);
    }

    /**
     * Validate JWT token
     * @param token the JWT token
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            log.debug("Validating JWT token");

            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            log.debug("JWT token is valid");
            return true;
        } catch (Exception e) {
            log.warn("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
