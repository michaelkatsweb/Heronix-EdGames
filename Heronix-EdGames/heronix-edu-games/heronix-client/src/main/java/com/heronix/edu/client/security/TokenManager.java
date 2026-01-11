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
            throw new TokenExpiredException("Token expired or not found");
        }
        return token;
    }

    /**
     * Check if token is valid and not expired
     */
    public boolean isTokenValid() {
        return token != null &&
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
