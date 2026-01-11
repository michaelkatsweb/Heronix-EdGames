package com.heronix.edu.server.dto.response;

import java.time.LocalDateTime;

/**
 * Standard error response DTO.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path);
    }
}
