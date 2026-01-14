package com.heronix.edu.client.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Manages JWT token storage and validation
 * Tokens are also persisted in the device table via DeviceRepository
 */
public class TokenManager {
    private static final Logger logger = LoggerFactory.getLogger(TokenManager.class);

    private String token;
    private LocalDateTime expiresAt;

    /**
     * Save a new JWT token
     */
    public void saveToken(String token, LocalDateTime expiresAt) {
        // Validate token before saving
        if (token == null || token.trim().isEmpty()) {
            logger.warn("Attempted to save null or empty token - ignoring");
            return;
        }

        // Basic JWT format validation (should have 2 periods for standard JWT)
        if (!token.contains(".")) {
            logger.warn("Attempted to save invalid JWT format (no periods) - ignoring");
            return;
        }

        this.token = token;
        this.expiresAt = expiresAt;
        logger.info("Token saved, expires at: {}", expiresAt);
    }

    /**
     * Get the current token
     * @throws TokenExpiredException if token is expired or not present
     */
    public String getToken() {
        if (!isTokenValid()) {
            logger.debug("Token validation failed - token: {}, expiresAt: {}",
                token != null ? (token.isEmpty() ? "empty" : "present") : "null",
                expiresAt);
            throw new TokenExpiredException("Token expired or not found");
        }
        return token;
    }

    /**
     * Check if token is valid and not expired
     */
    public boolean isTokenValid() {
        return token != null &&
               !token.trim().isEmpty() &&
               token.contains(".") &&  // Basic JWT format check
               expiresAt != null &&
               LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Clear the token
     */
    public void clearToken() {
        this.token = null;
        this.expiresAt = null;
        logger.info("Token cleared");
    }

    /**
     * Get token expiration time
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
