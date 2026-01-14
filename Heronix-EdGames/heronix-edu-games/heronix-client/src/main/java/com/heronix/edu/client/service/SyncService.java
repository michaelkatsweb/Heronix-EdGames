package com.heronix.edu.client.service;

import com.heronix.edu.client.api.HeronixApiClient;
import com.heronix.edu.client.api.dto.GameScoreDto;
import com.heronix.edu.client.api.dto.SyncResponse;
import com.heronix.edu.client.config.AppConfig;
import com.heronix.edu.client.db.entity.LocalGameScore;
import com.heronix.edu.client.db.entity.SyncCheckpoint;
import com.heronix.edu.client.db.entity.SyncConflict;
import com.heronix.edu.client.db.entity.SyncLog;
import com.heronix.edu.client.db.entity.SyncLog.SyncType;
import com.heronix.edu.client.db.repository.SyncCheckpointRepository;
import com.heronix.edu.client.db.repository.SyncConflictRepository;
import com.heronix.edu.client.db.repository.SyncLogRepository;
import com.heronix.edu.client.util.NetworkMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Service for background synchronization of game scores.
 *
 * Features:
 * - Delta sync: Only syncs changes since last checkpoint
 * - Conflict detection: Identifies version mismatches between local and server
 * - Audit logging: Records all sync operations for debugging
 * - Callback support: Notifies UI of conflicts requiring resolution
 */
public class SyncService {
    private static final Logger logger = LoggerFactory.getLogger(SyncService.class);
    private static final String ENTITY_TYPE_GAME_SCORE = "game_score";

    public enum SyncStatus {
        IDLE, SYNCING, SUCCESS, ERROR, CONFLICT
    }

    private final ScoreService scoreService;
    private final DeviceService deviceService;
    private final HeronixApiClient apiClient;
    private final NetworkMonitor networkMonitor;

    // Enhanced sync repositories
    private final SyncLogRepository syncLogRepository;
    private final SyncConflictRepository conflictRepository;
    private final SyncCheckpointRepository checkpointRepository;

    private ScheduledExecutorService scheduler;
    private final AtomicBoolean isSyncing = new AtomicBoolean(false);
    private final AtomicReference<SyncStatus> currentStatus = new AtomicReference<>(SyncStatus.IDLE);
    private final AtomicReference<String> lastSyncMessage = new AtomicReference<>("");
    private final AtomicInteger pendingConflictCount = new AtomicInteger(0);

    // Callback for conflict notification (UI can register to be notified)
    private Consumer<List<SyncConflict>> conflictCallback;

    public SyncService(ScoreService scoreService, DeviceService deviceService,
                      HeronixApiClient apiClient, NetworkMonitor networkMonitor) {
        this.scoreService = scoreService;
        this.deviceService = deviceService;
        this.apiClient = apiClient;
        this.networkMonitor = networkMonitor;
        // Initialize enhanced sync repositories
        this.syncLogRepository = new SyncLogRepository();
        this.conflictRepository = new SyncConflictRepository();
        this.checkpointRepository = new SyncCheckpointRepository();
    }

    /**
     * Register a callback to be notified when conflicts are detected.
     * The UI can use this to prompt the user for resolution.
     */
    public void setConflictCallback(Consumer<List<SyncConflict>> callback) {
        this.conflictCallback = callback;
    }

    /**
     * Get count of pending conflicts requiring resolution
     */
    public int getPendingConflictCount() {
        return pendingConflictCount.get();
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

            // Refresh token if needed - this will authenticate if token is missing/expired
            try {
                deviceService.refreshTokenIfNeeded();
            } catch (Exception e) {
                logger.warn("Unable to obtain valid token for sync: {}. Scores will sync when token is available.", e.getMessage());
                currentStatus.set(SyncStatus.ERROR);
                lastSyncMessage.set("Authentication required - scores pending");
                return;
            }

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

    // ========================================================================
    // ENHANCED SYNC METHODS - Delta Sync & Conflict Detection
    // ========================================================================

    /**
     * Perform delta sync - only sync changes since last checkpoint.
     * More efficient than full sync for regular background operations.
     */
    public void performDeltaSync() {
        if (!isSyncing.compareAndSet(false, true)) {
            logger.debug("Sync already in progress");
            return;
        }

        SyncLog syncLog = new SyncLog(SyncType.DELTA);
        currentStatus.set(SyncStatus.SYNCING);
        lastSyncMessage.set("Delta syncing...");

        try {
            // Get checkpoint for game scores
            SyncCheckpoint checkpoint = checkpointRepository.getOrCreate(ENTITY_TYPE_GAME_SCORE);

            List<LocalGameScore> scoresToSync;
            if (checkpoint.isInitial()) {
                // First sync - get all unsynced
                logger.info("First sync - performing full sync");
                scoresToSync = scoreService.getUnsyncedScores(AppConfig.getSyncBatchSize());
            } else {
                // Delta sync - only get scores modified since last sync
                scoresToSync = scoreService.getScoresModifiedAfter(checkpoint.getLastSyncTimestamp());
                logger.info("Delta sync - {} scores modified since {}", scoresToSync.size(), checkpoint.getLastSyncTimestamp());
            }

            if (scoresToSync.isEmpty()) {
                logger.debug("No scores to sync");
                syncLog.setScoresUploaded(0);
                syncLog.complete(true);
                syncLogRepository.save(syncLog);
                currentStatus.set(SyncStatus.SUCCESS);
                lastSyncMessage.set("All scores synced");
                return;
            }

            // Perform sync with conflict detection
            SyncResult result = syncWithConflictDetection(scoresToSync);

            // Update checkpoint on success
            if (result.success) {
                LocalDateTime now = LocalDateTime.now();
                checkpointRepository.updateAfterSync(ENTITY_TYPE_GAME_SCORE, now, checkpoint.getLastSyncVersion() + 1, null);
            }

            // Update sync log
            syncLog.setScoresUploaded(result.syncedCount);
            syncLog.setScoresFailed(result.failedCount);
            syncLog.setConflictsDetected(result.conflictCount);
            syncLog.setConflictsResolved(result.autoResolvedCount);
            syncLog.complete(result.success);
            syncLogRepository.save(syncLog);

            // Update status
            if (result.conflictCount > 0 && result.conflictCount > result.autoResolvedCount) {
                currentStatus.set(SyncStatus.CONFLICT);
                pendingConflictCount.set(result.conflictCount - result.autoResolvedCount);
                lastSyncMessage.set(String.format("Synced %d, %d conflicts need resolution",
                    result.syncedCount, pendingConflictCount.get()));

                // Notify UI of conflicts
                notifyConflicts();
            } else if (result.success) {
                currentStatus.set(SyncStatus.SUCCESS);
                lastSyncMessage.set("Synced " + result.syncedCount + " score(s)");
            } else {
                currentStatus.set(SyncStatus.ERROR);
                lastSyncMessage.set("Sync failed: " + result.errorMessage);
            }

        } catch (Exception e) {
            logger.error("Delta sync error", e);
            syncLog.fail(e.getMessage());
            syncLogRepository.save(syncLog);
            currentStatus.set(SyncStatus.ERROR);
            lastSyncMessage.set("Sync error: " + e.getMessage());
        } finally {
            isSyncing.set(false);
        }
    }

    /**
     * Sync scores with conflict detection.
     * Returns detailed result including conflicts.
     */
    private SyncResult syncWithConflictDetection(List<LocalGameScore> scores) {
        SyncResult result = new SyncResult();

        try {
            // Refresh token if needed - ensure valid token before proceeding
            try {
                deviceService.refreshTokenIfNeeded();
            } catch (Exception e) {
                logger.warn("Unable to obtain valid token for sync: {}. Scores will sync when token is available.", e.getMessage());
                result.success = false;
                result.errorMessage = "Authentication required - token expired or invalid";
                result.failedCount = scores.size();
                return result;
            }

            String deviceId = deviceService.getDevice()
                .orElseThrow(() -> new IllegalStateException("Device not found"))
                .getDeviceId();

            // Convert to DTOs with version info
            List<GameScoreDto> scoreDtos = scores.stream()
                .map(this::toDtoWithVersion)
                .collect(Collectors.toList());

            // Upload to server
            SyncResponse response = apiClient.uploadScores(deviceId, scoreDtos);

            if (response.isSuccess()) {
                result.syncedCount = response.getSuccessCount();
                result.success = true;

                // Mark all as synced
                for (LocalGameScore score : scores) {
                    scoreService.markAsSyncedWithVersion(score.getScoreId(), score.getLocalVersion());
                }

                logger.info("Sync successful: {} scores uploaded", result.syncedCount);

            } else {
                // Check for conflicts in response
                List<String> errors = response.getErrors();
                if (errors != null) {
                    for (String error : errors) {
                        if (error.contains("CONFLICT") || error.contains("version mismatch")) {
                            result.conflictCount++;
                            // Parse conflict details and create conflict record
                            handleConflictFromError(error, scores);
                        }
                    }
                }

                // Some scores may have succeeded
                result.syncedCount = response.getSuccessCount();
                result.failedCount = response.getFailureCount();
                result.success = result.failedCount == 0;
                result.errorMessage = response.getMessage();
            }

        } catch (Exception e) {
            logger.error("Error during sync with conflict detection", e);
            result.success = false;
            result.errorMessage = e.getMessage();
            result.failedCount = scores.size();
        }

        return result;
    }

    /**
     * Handle a conflict error from the server response
     */
    private void handleConflictFromError(String error, List<LocalGameScore> scores) {
        // Parse error to extract score ID (format depends on server implementation)
        // For now, create a generic conflict record
        logger.warn("Conflict detected: {}", error);

        // Try to extract score ID from error message
        for (LocalGameScore score : scores) {
            if (error.contains(score.getScoreId())) {
                createConflict(score, error);
                scoreService.markAsConflict(score.getScoreId());
                break;
            }
        }
    }

    /**
     * Create a conflict record for a score
     */
    private void createConflict(LocalGameScore localScore, String serverInfo) {
        SyncConflict conflict = SyncConflict.create(
            ENTITY_TYPE_GAME_SCORE,
            localScore.getScoreId(),
            SyncConflict.ConflictType.VERSION_MISMATCH
        );

        conflict.setLocalVersion(localScore.getLocalVersion());
        conflict.setLocalTimestamp(localScore.getModifiedAt());
        conflict.setLocalValue(String.format("Score: %d, Completed: %s",
            localScore.getScore(), localScore.isCompleted()));
        conflict.setServerValue(serverInfo);

        conflictRepository.save(conflict);
        logger.info("Created conflict record for score: {}", localScore.getScoreId());
    }

    /**
     * Notify registered callback about pending conflicts
     */
    private void notifyConflicts() {
        if (conflictCallback != null) {
            List<SyncConflict> pendingConflicts = conflictRepository.findPendingConflicts();
            if (!pendingConflicts.isEmpty()) {
                conflictCallback.accept(pendingConflicts);
            }
        }
    }

    /**
     * Resolve a conflict with the specified resolution
     */
    public void resolveConflict(Long conflictId, SyncConflict.Resolution resolution) {
        SyncConflict conflict = conflictRepository.findById(conflictId)
            .orElseThrow(() -> new IllegalArgumentException("Conflict not found: " + conflictId));

        switch (resolution) {
            case KEEP_LOCAL:
                // Re-attempt sync with force flag
                scoreService.resetSyncStatus(conflict.getEntityId());
                break;
            case KEEP_SERVER:
                // Mark local as synced (server version wins)
                scoreService.markAsSynced(conflict.getEntityId());
                break;
            case MERGE:
                // For game scores, merging doesn't make sense - default to server wins
                scoreService.markAsSynced(conflict.getEntityId());
                break;
            default:
                break;
        }

        conflictRepository.updateResolution(conflictId, resolution, "user");
        pendingConflictCount.decrementAndGet();

        logger.info("Conflict {} resolved as {}", conflictId, resolution);
    }

    /**
     * Auto-resolve all conflicts using server-wins strategy
     */
    public int autoResolveAllConflicts() {
        List<SyncConflict> pending = conflictRepository.findPendingConflicts();
        int resolved = 0;

        for (SyncConflict conflict : pending) {
            try {
                resolveConflict(conflict.getId(), SyncConflict.Resolution.KEEP_SERVER);
                resolved++;
            } catch (Exception e) {
                logger.error("Failed to auto-resolve conflict {}: {}", conflict.getId(), e.getMessage());
            }
        }

        logger.info("Auto-resolved {} conflicts using server-wins strategy", resolved);
        return resolved;
    }

    /**
     * Get list of pending conflicts for UI display
     */
    public List<SyncConflict> getPendingConflicts() {
        return conflictRepository.findPendingConflicts();
    }

    /**
     * Get sync statistics
     */
    public SyncStatistics getSyncStatistics() {
        SyncLogRepository.SyncStats stats = syncLogRepository.getStats();
        return new SyncStatistics(
            stats.totalSyncs,
            stats.successfulSyncs,
            stats.totalScoresUploaded,
            stats.totalBytesTransferred,
            conflictRepository.countPending(),
            scoreService.getUnsyncedCount()
        );
    }

    /**
     * Get recent sync history
     */
    public List<SyncLog> getRecentSyncHistory(int limit) {
        return syncLogRepository.findRecent(limit);
    }

    /**
     * Force a full sync (reset checkpoint and sync everything)
     */
    public void forceFullSync() {
        logger.info("Forcing full sync - resetting checkpoint");
        checkpointRepository.reset(ENTITY_TYPE_GAME_SCORE);
        performDeltaSync();
    }

    /**
     * Convert LocalGameScore to DTO with version information
     */
    private GameScoreDto toDtoWithVersion(LocalGameScore score) {
        GameScoreDto dto = toDto(score);
        // Add version info to metadata if needed
        // The server can use this for conflict detection
        return dto;
    }

    // ========================================================================
    // RESULT CLASSES
    // ========================================================================

    /**
     * Internal result class for sync operations
     */
    private static class SyncResult {
        boolean success;
        int syncedCount;
        int failedCount;
        int conflictCount;
        int autoResolvedCount;
        String errorMessage;
    }

    /**
     * Public statistics class for UI display
     */
    public static class SyncStatistics {
        public final int totalSyncs;
        public final int successfulSyncs;
        public final long totalScoresUploaded;
        public final long totalBytesTransferred;
        public final int pendingConflicts;
        public final int pendingScores;

        public SyncStatistics(int totalSyncs, int successfulSyncs, long totalScoresUploaded,
                             long totalBytesTransferred, int pendingConflicts, int pendingScores) {
            this.totalSyncs = totalSyncs;
            this.successfulSyncs = successfulSyncs;
            this.totalScoresUploaded = totalScoresUploaded;
            this.totalBytesTransferred = totalBytesTransferred;
            this.pendingConflicts = pendingConflicts;
            this.pendingScores = pendingScores;
        }

        public double getSuccessRate() {
            return totalSyncs > 0 ? (double) successfulSyncs / totalSyncs * 100 : 0;
        }
    }
}
