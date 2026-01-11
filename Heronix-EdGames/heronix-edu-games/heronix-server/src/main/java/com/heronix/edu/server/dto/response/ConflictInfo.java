package com.heronix.edu.server.dto.response;

/**
 * Information about a sync conflict.
 */
public record ConflictInfo(
        String scoreId,
        ConflictType type,
        String resolution,
        String message
) {
}
