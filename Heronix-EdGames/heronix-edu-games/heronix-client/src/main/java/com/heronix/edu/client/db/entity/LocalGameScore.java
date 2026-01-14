package com.heronix.edu.client.db.entity;

import java.time.LocalDateTime;

/**
 * Local entity representing a game score
 * Queued for sync with the server
 */
public class LocalGameScore {
    private String scoreId;
    private String studentId;
    private String gameId;
    private int score;
    private int maxScore;
    private Integer timeSeconds;
    private Integer correctAnswers;
    private Integer incorrectAnswers;
    private Integer completionPercentage;
    private boolean completed;
    private String difficultyLevel;
    private LocalDateTime playedAt;
    private String deviceId;
    private boolean synced;
    private LocalDateTime syncedAt;
    private int syncAttempts;
    private String lastSyncError;
    private String metadata; // JSON

    // Delta sync fields
    private int localVersion = 1;      // Increments on each local update
    private int serverVersion = 0;     // Last known server version
    private LocalDateTime modifiedAt;
    private String contentHash;        // SHA-256 of content for change detection
    private String syncStatus = "PENDING"; // PENDING, SYNCED, CONFLICT, ERROR

    // Constructors
    public LocalGameScore() {
        this.modifiedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getScoreId() {
        return scoreId;
    }

    public void setScoreId(String scoreId) {
        this.scoreId = scoreId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public Integer getTimeSeconds() {
        return timeSeconds;
    }

    public void setTimeSeconds(Integer timeSeconds) {
        this.timeSeconds = timeSeconds;
    }

    public Integer getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Integer correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Integer getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public void setIncorrectAnswers(Integer incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
    }

    public Integer getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(Integer completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(LocalDateTime playedAt) {
        this.playedAt = playedAt;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public LocalDateTime getSyncedAt() {
        return syncedAt;
    }

    public void setSyncedAt(LocalDateTime syncedAt) {
        this.syncedAt = syncedAt;
    }

    public int getSyncAttempts() {
        return syncAttempts;
    }

    public void setSyncAttempts(int syncAttempts) {
        this.syncAttempts = syncAttempts;
    }

    public String getLastSyncError() {
        return lastSyncError;
    }

    public void setLastSyncError(String lastSyncError) {
        this.lastSyncError = lastSyncError;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    // Delta sync getters and setters
    public int getLocalVersion() {
        return localVersion;
    }

    public void setLocalVersion(int localVersion) {
        this.localVersion = localVersion;
    }

    public int getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(int serverVersion) {
        this.serverVersion = serverVersion;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    /**
     * Increment local version (call when entity is modified)
     */
    public void incrementLocalVersion() {
        this.localVersion++;
        this.modifiedAt = LocalDateTime.now();
    }

    /**
     * Check if this score has unsynced changes
     */
    public boolean hasUnsyncedChanges() {
        return localVersion > serverVersion || "PENDING".equals(syncStatus) || "CONFLICT".equals(syncStatus);
    }

    @Override
    public String toString() {
        return "LocalGameScore{" +
                "scoreId='" + scoreId + '\'' +
                ", gameId='" + gameId + '\'' +
                ", score=" + score +
                ", synced=" + synced +
                ", syncStatus='" + syncStatus + '\'' +
                ", localVersion=" + localVersion +
                ", serverVersion=" + serverVersion +
                '}';
    }
}
