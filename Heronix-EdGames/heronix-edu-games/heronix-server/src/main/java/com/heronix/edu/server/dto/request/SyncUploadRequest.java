package com.heronix.edu.server.dto.request;

import com.heronix.edu.common.model.GameScore;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO for score synchronization upload.
 */
public record SyncUploadRequest(
        @NotNull(message = "Scores list cannot be null")
        @NotEmpty(message = "Scores list cannot be empty")
        List<GameScore> scores
) {
}
