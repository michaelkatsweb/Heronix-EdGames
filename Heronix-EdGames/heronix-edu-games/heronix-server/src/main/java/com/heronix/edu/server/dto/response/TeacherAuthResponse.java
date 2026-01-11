package com.heronix.edu.server.dto.response;

import java.time.LocalDateTime;

/**
 * Response DTO for teacher authentication.
 */
public record TeacherAuthResponse(
        String token,
        String tokenType,
        LocalDateTime expiresAt,
        Long userId,
        String username,
        String role
) {
    public TeacherAuthResponse(String token, LocalDateTime expiresAt, Long userId, String username, String role) {
        this(token, "Bearer", expiresAt, userId, username, role);
    }
}
