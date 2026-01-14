package com.heronix.edu.client.service;

import com.heronix.edu.client.db.entity.LocalGameScore;
import com.heronix.edu.client.db.repository.GameScoreRepository;
import com.heronix.edu.common.game.GameResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing game scores
 * Handles saving scores locally and queuing for sync
 */
public class ScoreService {
    private static final Logger logger = LoggerFactory.getLogger(ScoreService.class);

    private final GameScoreRepository scoreRepository;
    private final DeviceService deviceService;

    public ScoreService(GameScoreRepository scoreRepository, DeviceService deviceService) {
        this.scoreRepository = scoreRepository;
        this.deviceService = deviceService;
    }

    /**
     * Save a game score from a GameResult
     * Score is marked as unsynced and will be uploaded by SyncService
     */
    public void saveGameScore(GameResult result) {
        logger.info("Saving game score: {} - {}/{}",
            result.getGameId(), result.getScore(), result.getMaxScore());

        try {
            // Get student ID from device
            String studentId = deviceService.getStudentId();
            String deviceId = deviceService.getDevice()
                .orElseThrow(() -> new IllegalStateException("Device not found"))
                .getDeviceId();

            // Create local score entity
            LocalGameScore score = new LocalGameScore();
            score.setScoreId(UUID.randomUUID().toString());
            score.setStudentId(studentId);
            score.setGameId(result.getGameId());
            score.setScore(result.getScore());
            score.setMaxScore(result.getMaxScore());
            score.setTimeSeconds(result.getTimeElapsedSeconds());
            score.setCorrectAnswers(result.getCorrectAnswers());
            score.setIncorrectAnswers(result.getIncorrectAnswers());
            score.setCompletionPercentage(result.getCompletionPercentage());
            score.setCompleted(result.isCompleted());
            score.setDifficultyLevel(result.getDifficultyLevel() != null ? result.getDifficultyLevel().toString() : null);
            score.setPlayedAt(LocalDateTime.now());
            score.setDeviceId(deviceId);
            score.setSynced(false); // Will be synced by SyncService
            score.setSyncAttempts(0);
            score.setMetadata(null); // GameResult doesn't have getMetadata() in this version

            // Save to database
            scoreRepository.save(score);

            logger.info("Game score saved successfully: {}", score.getScoreId());

        } catch (Exception e) {
            logger.error("Failed to save game score", e);
            throw new RuntimeException("Failed to save score", e);
        }
    }

    /**
     * Get all unsynced scores
     */
    public List<LocalGameScore> getUnsyncedScores() {
        return scoreRepository.findUnsyncedScores();
    }

    /**
     * Get unsynced scores with limit
     */
    public List<LocalGameScore> getUnsyncedScores(int limit) {
        return scoreRepository.findUnsyncedScores(limit);
    }

    /**
     * Mark a score as synced
     */
    public void markAsSynced(String scoreId) {
        scoreRepository.markAsSynced(scoreId);
    }

    /**
     * Increment sync attempt counter
     */
    public void incrementSyncAttempt(String scoreId, String errorMessage) {
        scoreRepository.incrementSyncAttempt(scoreId, errorMessage);
    }

    /**
     * Get count of unsynced scores
     */
    public int getUnsyncedCount() {
        return scoreRepository.countUnsyncedScores();
    }

    /**
     * Get recent scores (most recent first)
     */
    public List<LocalGameScore> getRecentScores(int limit) {
        return scoreRepository.findRecentScores(limit);
    }

    /**
     * Get all scores
     */
    public List<LocalGameScore> getAllScores() {
        return scoreRepository.findAll();
    }

    // ========================================================================
    // DELTA SYNC METHODS
    // ========================================================================

    /**
     * Get scores modified after a given timestamp (for delta sync)
     */
    public List<LocalGameScore> getScoresModifiedAfter(LocalDateTime since) {
        return scoreRepository.findModifiedAfter(since);
    }

    /**
     * Mark a score as synced with a specific server version
     */
    public void markAsSyncedWithVersion(String scoreId, int serverVersion) {
        scoreRepository.markAsSyncedWithVersion(scoreId, serverVersion);
    }

    /**
     * Mark a score as having a conflict
     */
    public void markAsConflict(String scoreId) {
        scoreRepository.markAsConflict(scoreId, 0);
    }

    /**
     * Reset sync status for a score (to retry sync)
     */
    public void resetSyncStatus(String scoreId) {
        scoreRepository.updateSyncStatus(scoreId, "PENDING", 0);
    }

    /**
     * Get count of scores with conflicts
     */
    public int getConflictCount() {
        return scoreRepository.countConflicts();
    }

    /**
     * Get all conflicting scores
     */
    public List<LocalGameScore> getConflictingScores() {
        return scoreRepository.findConflictingScores();
    }

    /**
     * Get scores by sync status
     */
    public List<LocalGameScore> getScoresBySyncStatus(String status) {
        return scoreRepository.findBySyncStatus(status);
    }
}
