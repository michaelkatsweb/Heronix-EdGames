package com.heronix.edu.client.db.entity;

import java.time.LocalDateTime;

/**
 * Entity representing a sync operation log entry.
 * Tracks sync history for auditing and debugging.
 */
public class SyncLog {

    public enum SyncType {
        FULL,       // Full sync of all pending items
        DELTA,      // Only sync changed items since last checkpoint
        MANUAL      // User-triggered manual sync
    }

    private Long id;
    private LocalDateTime syncStartedAt;
    private LocalDateTime syncCompletedAt;
    private SyncType syncType;
    private int scoresUploaded;
    private int scoresFailed;
    private long bytesTransferred;
    private boolean success;
    private String errorMessage;
    private int conflictsDetected;
    private int conflictsResolved;

    public SyncLog() {
        this.syncStartedAt = LocalDateTime.now();
        this.syncType = SyncType.FULL;
    }

    public SyncLog(SyncType type) {
        this.syncStartedAt = LocalDateTime.now();
        this.syncType = type;
    }

    /**
     * Mark this sync as completed with success/failure
     */
    public void complete(boolean success) {
        this.syncCompletedAt = LocalDateTime.now();
        this.success = success;
    }

    /**
     * Mark as failed with error message
     */
    public void fail(String errorMessage) {
        this.syncCompletedAt = LocalDateTime.now();
        this.success = false;
        this.errorMessage = errorMessage;
    }

    /**
     * Get duration of sync in milliseconds
     */
    public long getDurationMs() {
        if (syncStartedAt == null || syncCompletedAt == null) {
            return 0;
        }
        return java.time.Duration.between(syncStartedAt, syncCompletedAt).toMillis();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getSyncStartedAt() {
        return syncStartedAt;
    }

    public void setSyncStartedAt(LocalDateTime syncStartedAt) {
        this.syncStartedAt = syncStartedAt;
    }

    public LocalDateTime getSyncCompletedAt() {
        return syncCompletedAt;
    }

    public void setSyncCompletedAt(LocalDateTime syncCompletedAt) {
        this.syncCompletedAt = syncCompletedAt;
    }

    public SyncType getSyncType() {
        return syncType;
    }

    public void setSyncType(SyncType syncType) {
        this.syncType = syncType;
    }

    public int getScoresUploaded() {
        return scoresUploaded;
    }

    public void setScoresUploaded(int scoresUploaded) {
        this.scoresUploaded = scoresUploaded;
    }

    public int getScoresFailed() {
        return scoresFailed;
    }

    public void setScoresFailed(int scoresFailed) {
        this.scoresFailed = scoresFailed;
    }

    public long getBytesTransferred() {
        return bytesTransferred;
    }

    public void setBytesTransferred(long bytesTransferred) {
        this.bytesTransferred = bytesTransferred;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getConflictsDetected() {
        return conflictsDetected;
    }

    public void setConflictsDetected(int conflictsDetected) {
        this.conflictsDetected = conflictsDetected;
    }

    public int getConflictsResolved() {
        return conflictsResolved;
    }

    public void setConflictsResolved(int conflictsResolved) {
        this.conflictsResolved = conflictsResolved;
    }

    @Override
    public String toString() {
        return "SyncLog{" +
                "id=" + id +
                ", syncType=" + syncType +
                ", scoresUploaded=" + scoresUploaded +
                ", success=" + success +
                ", duration=" + getDurationMs() + "ms" +
                '}';
    }
}
