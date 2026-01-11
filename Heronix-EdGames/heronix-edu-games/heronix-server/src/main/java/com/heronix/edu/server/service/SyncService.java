package com.heronix.edu.server.service;

import com.heronix.edu.common.model.GameScore;
import com.heronix.edu.server.dto.response.ConflictInfo;
import com.heronix.edu.server.dto.response.ConflictType;
import com.heronix.edu.server.dto.response.SyncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for score synchronization.
 * Handles batch score uploads with conflict detection and resolution.
 */
@Service
public class SyncService {

    private static final Logger logger = LoggerFactory.getLogger(SyncService.class);

    @Autowired
    private GameScoreService gameScoreService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private AuditService auditService;

    @Value("${heronix.sync.batch-size:100}")
    private int batchSize;

    /**
     * Upload and process game scores from device
     */
    @Transactional
    public SyncResponse uploadScores(String deviceId, List<GameScore> scores) {
        logger.info("Processing score upload from device: {} ({} scores)", deviceId, scores.size());

        List<ConflictInfo> conflicts = new ArrayList<>();
        List<GameScore> validScores = new ArrayList<>();

        int scoresProcessed = 0;
        int scoresAccepted = 0;
        int scoresRejected = 0;

        // Validate device is approved
        if (!deviceService.isDeviceApproved(deviceId)) {
            logger.warn("Upload rejected - device not approved: {}", deviceId);
            auditService.logScoreSync(deviceId, 0, "FAILURE");

            return new SyncResponse(
                    false,
                    0,
                    0,
                    scores.size(),
                    List.of(new ConflictInfo(
                            null,
                            ConflictType.DEVICE_NOT_AUTHORIZED,
                            "REJECTED",
                            "Device is not approved for sync"
                    )),
                    UUID.randomUUID().toString(),
                    LocalDateTime.now()
            );
        }

        // Process each score
        for (GameScore score : scores) {
            scoresProcessed++;

            // Validate score
            ValidationResult validation = validateScore(score, deviceId);
            if (!validation.isValid()) {
                scoresRejected++;
                conflicts.add(new ConflictInfo(
                        score.getScoreId(),
                        ConflictType.VALIDATION_ERROR,
                        "REJECTED",
                        validation.message()
                ));
                logger.debug("Score validation failed: {} - {}", score.getScoreId(), validation.message());
                continue;
            }

            // Check for duplicate score
            if (gameScoreService.scoreExists(score.getScoreId())) {
                scoresRejected++;
                conflicts.add(new ConflictInfo(
                        score.getScoreId(),
                        ConflictType.DUPLICATE_SCORE,
                        "SERVER_KEPT",
                        "Score already exists on server"
                ));
                logger.debug("Duplicate score detected: {}", score.getScoreId());
                continue;
            }

            // Score is valid and not duplicate
            validScores.add(score);
            scoresAccepted++;
        }

        // Save valid scores in batch
        if (!validScores.isEmpty()) {
            try {
                gameScoreService.saveScores(validScores);
                logger.info("Saved {} valid scores", validScores.size());
            } catch (Exception e) {
                logger.error("Error saving scores", e);
                auditService.logScoreSync(deviceId, 0, "ERROR");
                throw new RuntimeException("Failed to save scores", e);
            }
        }

        // Update device last sync time
        deviceService.updateLastSync(deviceId, LocalDateTime.now());

        // Log audit event
        auditService.logScoreSync(deviceId, scoresAccepted, "SUCCESS");

        String syncId = UUID.randomUUID().toString();

        logger.info("Sync completed for device: {} - Processed: {}, Accepted: {}, Rejected: {}",
                deviceId, scoresProcessed, scoresAccepted, scoresRejected);

        return new SyncResponse(
                true,
                scoresProcessed,
                scoresAccepted,
                scoresRejected,
                conflicts,
                syncId,
                LocalDateTime.now()
        );
    }

    /**
     * Get last sync timestamp for a device
     */
    @Transactional(readOnly = true)
    public LocalDateTime getLastSyncTimestamp(String deviceId) {
        logger.debug("Fetching last sync timestamp for device: {}", deviceId);

        var device = deviceService.getDevice(deviceId);
        return device.getLastSyncAt();
    }

    /**
     * Mark sync as complete
     */
    @Transactional
    public void markSyncComplete(String deviceId) {
        logger.debug("Marking sync complete for device: {}", deviceId);

        deviceService.updateLastSync(deviceId, LocalDateTime.now());
    }

    /**
     * Validate a game score
     */
    private ValidationResult validateScore(GameScore score, String deviceId) {
        // Check score has required fields
        if (score.getScoreId() == null || score.getScoreId().isBlank()) {
            return new ValidationResult(false, "Score ID is required");
        }

        if (score.getStudentId() == null || score.getStudentId().isBlank()) {
            return new ValidationResult(false, "Student ID is required");
        }

        if (score.getGameId() == null || score.getGameId().isBlank()) {
            return new ValidationResult(false, "Game ID is required");
        }

        // Validate score range
        if (score.getScore() < 0 || score.getScore() > score.getMaxScore()) {
            return new ValidationResult(false,
                    String.format("Score %d is out of range (0-%d)", score.getScore(), score.getMaxScore()));
        }

        // Validate student exists and can participate
        if (!studentService.studentExists(score.getStudentId())) {
            return new ValidationResult(false, "Student not found: " + score.getStudentId());
        }

        if (!studentService.canParticipate(score.getStudentId())) {
            return new ValidationResult(false, "Student not eligible to participate");
        }

        // Validate device matches score
        if (score.getDeviceId() != null && !score.getDeviceId().equals(deviceId)) {
            return new ValidationResult(false, "Device ID mismatch");
        }

        return new ValidationResult(true, "Valid");
    }

    /**
     * Internal record for validation results
     */
    private record ValidationResult(boolean isValid, String message) {
    }
}
