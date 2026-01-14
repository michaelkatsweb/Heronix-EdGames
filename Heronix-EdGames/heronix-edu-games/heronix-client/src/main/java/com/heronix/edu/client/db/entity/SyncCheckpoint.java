package com.heronix.edu.client.db.entity;

import java.time.LocalDateTime;

/**
 * Entity representing a delta sync checkpoint.
 * Tracks the last successful sync point for efficient delta syncing.
 */
public class SyncCheckpoint {

    private String id;                  // Entity type: 'game_score', 'student', etc.
    private LocalDateTime lastSyncTimestamp;
    private long lastSyncVersion;
    private String serverSequenceId;    // Server's sequence marker for ordering
    private LocalDateTime updatedAt;

    public SyncCheckpoint() {
        this.lastSyncVersion = 0;
        this.updatedAt = LocalDateTime.now();
    }

    public SyncCheckpoint(String entityType) {
        this.id = entityType;
        this.lastSyncVersion = 0;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Update checkpoint after successful sync
     */
    public void updateCheckpoint(LocalDateTime timestamp, long version, String serverSequenceId) {
        this.lastSyncTimestamp = timestamp;
        this.lastSyncVersion = version;
        this.serverSequenceId = serverSequenceId;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if this is a fresh checkpoint (never synced)
     */
    public boolean isInitial() {
        return lastSyncTimestamp == null && lastSyncVersion == 0;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getLastSyncTimestamp() {
        return lastSyncTimestamp;
    }

    public void setLastSyncTimestamp(LocalDateTime lastSyncTimestamp) {
        this.lastSyncTimestamp = lastSyncTimestamp;
    }

    public long getLastSyncVersion() {
        return lastSyncVersion;
    }

    public void setLastSyncVersion(long lastSyncVersion) {
        this.lastSyncVersion = lastSyncVersion;
    }

    public String getServerSequenceId() {
        return serverSequenceId;
    }

    public void setServerSequenceId(String serverSequenceId) {
        this.serverSequenceId = serverSequenceId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "SyncCheckpoint{" +
                "id='" + id + '\'' +
                ", lastSyncTimestamp=" + lastSyncTimestamp +
                ", lastSyncVersion=" + lastSyncVersion +
                '}';
    }
}
