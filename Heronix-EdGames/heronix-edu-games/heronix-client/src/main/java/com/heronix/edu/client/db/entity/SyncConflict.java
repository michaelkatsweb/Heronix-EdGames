package com.heronix.edu.client.db.entity;

import java.time.LocalDateTime;

/**
 * Entity representing a sync conflict between local and server data.
 * Used for conflict resolution UI and tracking.
 */
public class SyncConflict {

    public enum ConflictType {
        UPDATE_CONFLICT,    // Both local and server modified same entity
        DELETE_CONFLICT,    // Entity deleted on one side, modified on other
        VERSION_MISMATCH    // Version numbers don't match expected
    }

    public enum Resolution {
        PENDING,       // Not yet resolved
        KEEP_LOCAL,    // Use local version
        KEEP_SERVER,   // Use server version
        MERGE          // Attempt to merge both versions
    }

    private Long id;
    private String entityType;      // 'game_score', 'student', etc.
    private String entityId;
    private String fieldName;       // Specific field in conflict (null = entire entity)
    private String localValue;
    private String serverValue;
    private Integer localVersion;
    private Integer serverVersion;
    private LocalDateTime localTimestamp;
    private LocalDateTime serverTimestamp;
    private ConflictType conflictType;
    private Resolution resolution;
    private LocalDateTime resolvedAt;
    private String resolvedBy;      // 'auto', 'user', 'server_wins'
    private LocalDateTime createdAt;
    private String metadata;        // JSON for additional context

    public SyncConflict() {
        this.createdAt = LocalDateTime.now();
        this.resolution = Resolution.PENDING;
    }

    // Builder-style factory method
    public static SyncConflict create(String entityType, String entityId, ConflictType type) {
        SyncConflict conflict = new SyncConflict();
        conflict.setEntityType(entityType);
        conflict.setEntityId(entityId);
        conflict.setConflictType(type);
        return conflict;
    }

    /**
     * Check if server version is newer based on timestamps
     */
    public boolean isServerNewer() {
        if (localTimestamp == null || serverTimestamp == null) {
            return serverVersion != null && localVersion != null && serverVersion > localVersion;
        }
        return serverTimestamp.isAfter(localTimestamp);
    }

    /**
     * Check if this conflict has been resolved
     */
    public boolean isResolved() {
        return resolution != null && resolution != Resolution.PENDING;
    }

    /**
     * Resolve with a specific resolution type
     */
    public void resolve(Resolution resolution, String resolvedBy) {
        this.resolution = resolution;
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = resolvedBy;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getLocalValue() {
        return localValue;
    }

    public void setLocalValue(String localValue) {
        this.localValue = localValue;
    }

    public String getServerValue() {
        return serverValue;
    }

    public void setServerValue(String serverValue) {
        this.serverValue = serverValue;
    }

    public Integer getLocalVersion() {
        return localVersion;
    }

    public void setLocalVersion(Integer localVersion) {
        this.localVersion = localVersion;
    }

    public Integer getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(Integer serverVersion) {
        this.serverVersion = serverVersion;
    }

    public LocalDateTime getLocalTimestamp() {
        return localTimestamp;
    }

    public void setLocalTimestamp(LocalDateTime localTimestamp) {
        this.localTimestamp = localTimestamp;
    }

    public LocalDateTime getServerTimestamp() {
        return serverTimestamp;
    }

    public void setServerTimestamp(LocalDateTime serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }

    public ConflictType getConflictType() {
        return conflictType;
    }

    public void setConflictType(ConflictType conflictType) {
        this.conflictType = conflictType;
    }

    public Resolution getResolution() {
        return resolution;
    }

    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "SyncConflict{" +
                "entityType='" + entityType + '\'' +
                ", entityId='" + entityId + '\'' +
                ", conflictType=" + conflictType +
                ", resolution=" + resolution +
                '}';
    }
}
