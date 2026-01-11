package com.heronix.edu.server.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for score synchronization.
 */
public record SyncResponse(
        boolean success,
        int scoresProcessed,
        int scoresAccepted,
        int scoresRejected,
        List<ConflictInfo> conflicts,
        String syncId,
        LocalDateTime syncTimestamp
) {
}
