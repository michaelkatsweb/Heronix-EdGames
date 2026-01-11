package com.heronix.edu.server.dto.response;

import java.time.LocalDateTime;

/**
 * Response DTO for authentication.
 */
public record AuthResponse(
        String token,
        String tokenType,
        LocalDateTime expiresAt,
        String deviceId,
        String studentId
) {
    public AuthResponse(String token, LocalDateTime expiresAt, String deviceId, String studentId) {
        this(token, "Bearer", expiresAt, deviceId, studentId);
    }
}
