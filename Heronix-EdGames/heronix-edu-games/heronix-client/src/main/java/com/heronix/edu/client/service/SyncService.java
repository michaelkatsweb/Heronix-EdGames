package com.heronix.edu.client.service;

import com.heronix.edu.client.api.HeronixApiClient;
import com.heronix.edu.client.api.dto.GameScoreDto;
import com.heronix.edu.client.api.dto.SyncResponse;
import com.heronix.edu.client.config.AppConfig;
import com.heronix.edu.client.db.entity.LocalGameScore;
import com.heronix.edu.client.util.NetworkMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Service for background synchronization of game scores
 * Periodically uploads unsynced scores to the server
 */
public class SyncService {
    private static final Logger logger = LoggerFactory.getLogger(SyncService.class);

    public enum SyncStatus {
        IDLE, SYNCING, SUCCESS, ERROR
    }

    private final ScoreService scoreService;
    private final DeviceService deviceService;
    private final HeronixApiClient apiClient;
    private final NetworkMonitor networkMonitor;

    private ScheduledExecutorService scheduler;
    private final AtomicBoolean isSyncing = new AtomicBoolean(false);
    private final AtomicReference<SyncStatus> currentStatus = new AtomicReference<>(SyncStatus.IDLE);
    private final AtomicReference<String> lastSyncMessage = new AtomicReference<>("");

    public SyncService(ScoreService scoreService, DeviceService deviceService,
                      HeronixApiClient apiClient, NetworkMonitor networkMonitor) {
        this.scoreService = scoreService;
        this.deviceService = deviceService;
        this.apiClient = apiClient;
        this.networkMonitor = networkMonitor;
    }

    /**
     * Start background sync scheduler
     */
    public void startBackgroundSync() {
        if (scheduler != null && !scheduler.isShutdown()) {
            logger.warn("Background sync already running");
            return;
        }

        int intervalMinutes = AppConfig.getSyncIntervalMinutes();
        logger.info("Starting background sync (every {} minutes)", intervalMinutes);

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SyncService");
            t.setDaemon(true);
            return t;
        });

        // Initial sync after 30 seconds
        scheduler.schedule(this::performSyncIfOnline, 30, TimeUnit.SECONDS);

        // Periodic sync
        scheduler.scheduleAtFixedRate(
            this::performSyncIfOnline,
            intervalMinutes,
            intervalMinutes,
            TimeUnit.MINUTES
        );

        logger.info("Background sync started");
    }

    /**
     * Stop background sync
     */
    public void stopBackgroundSync() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            logger.info("Background sync stopped");
        }
    }

    /**
     * Perform sync only if online
     */
    private void performSyncIfOnline() {
        if (!networkMonitor.isOnline()) {
            logger.debug("Skipping sync - offline");
            return;
        }

        performSync();
    }

    /**
     * Perform synchronization of unsynced scores
     */
    public void performSync() {
        // Check if already syncing
        if (!isSyncing.compareAndSet(false, true)) {
            logger.debug("Sync already in progress");
            return;
        }

        currentStatus.set(SyncStatus.SYNCING);
        lastSyncMessage.set("Syncing...");

        try {
            // Get unsynced scores
            int batchSize = AppConfig.getSyncBatchSize();
            List<LocalGameScore> unsyncedScores = scoreService.getUnsyncedScores(batchSize);

            if (unsyncedScores.isEmpty()) {
                logger.debug("No scores to sync");
                currentStatus.set(SyncStatus.SUCCESS);
                lastSyncMessage.set("All scores synced");
                return;
            }

            logger.info("Syncing {} scores to server", unsyncedScores.size());

            // Convert to DTOs
            List<GameScoreDto> scoreDtos = unsyncedScores.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

            // Get device ID
            String deviceId = deviceService.getDevice()
                .orElseThrow(() -> new IllegalStateException("Device not found"))
                .getDeviceId();

            // Refresh token if needed
            deviceService.refreshTokenIfNeeded();

            // Upload to server
            SyncResponse response = apiClient.uploadScores(deviceId, scoreDtos);

            // Process response
            if (response.isSuccess()) {
                // Mark all as synced (server wins approach)
                for (LocalGameScore score : unsyncedScores) {
                    scoreService.markAsSynced(score.getScoreId());
                }

                currentStatus.set(SyncStatus.SUCCESS);
                lastSyncMessage.set("Synced " + response.getSuccessCount() + " score(s)");
                logger.info("Sync successful: {} scores uploaded", response.getSuccessCount());

            } else {
                // Handle partial or full failure
                currentStatus.set(SyncStatus.ERROR);
                lastSyncMessage.set("Sync failed: " + response.getMessage());
                logger.error("Sync failed: {}", response.getMessage());

                // Increment sync attempts for failed scores
                for (LocalGameScore score : unsyncedScores) {
                    String errorMsg = response.getMessage() != null ?
                        response.getMessage() : "Unknown error";
                    scoreService.incrementSyncAttempt(score.getScoreId(), errorMsg);
                }
            }

        } catch (Exception e) {
            logger.error("Sync error", e);
            currentStatus.set(SyncStatus.ERROR);
            lastSyncMessage.set("Sync error: " + e.getMessage());

            // Increment sync attempts
            List<LocalGameScore> unsyncedScores = scoreService.getUnsyncedScores(
                AppConfig.getSyncBatchSize()
            );
            for (LocalGameScore score : unsyncedScores) {
                scoreService.incrementSyncAttempt(score.getScoreId(), e.getMessage());
            }

        } finally {
            isSyncing.set(false);
        }
    }

    /**
     * Convert LocalGameScore to DTO
     */
    private GameScoreDto toDto(LocalGameScore score) {
        GameScoreDto dto = new GameScoreDto();
        dto.setScoreId(score.getScoreId());
        dto.setStudentId(score.getStudentId());
        dto.setGameId(score.getGameId());
        dto.setScore(score.getScore());
        dto.setMaxScore(score.getMaxScore());
        dto.setTimeSeconds(score.getTimeSeconds());
        dto.setCorrectAnswers(score.getCorrectAnswers());
        dto.setIncorrectAnswers(score.getIncorrectAnswers());
        dto.setCompletionPercentage(score.getCompletionPercentage());
        dto.setCompleted(score.isCompleted());
        dto.setDifficultyLevel(score.getDifficultyLevel());
        dto.setPlayedAt(score.getPlayedAt());
        dto.setDeviceId(score.getDeviceId());
        dto.setMetadata(score.getMetadata());
        return dto;
    }

    /**
     * Get current sync status
     */
    public SyncStatus getSyncStatus() {
        return currentStatus.get();
    }

    /**
     * Get last sync message
     */
    public String getLastSyncMessage() {
        return lastSyncMessage.get();
    }

    /**
     * Check if currently syncing
     */
    public boolean isSyncing() {
        return isSyncing.get();
    }

    /**
     * Get count of pending scores
     */
    public int getPendingCount() {
        return scoreService.getUnsyncedCount();
    }
}
