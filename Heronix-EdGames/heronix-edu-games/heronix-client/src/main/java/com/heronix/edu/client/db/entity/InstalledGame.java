package com.heronix.edu.client.db.entity;

import java.time.LocalDateTime;

/**
 * Local entity representing an installed game
 */
public class InstalledGame {
    private String gameId;
    private String gameName;
    private String description;
    private String version;
    private String subject;
    private String targetGrades; // JSON array
    private String jarPath;
    private String jarChecksum;
    private LocalDateTime installedAt;
    private LocalDateTime lastPlayedAt;
    private Long fileSizeBytes;

    // Constructors
    public InstalledGame() {
    }

    // Getters and Setters
    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTargetGrades() {
        return targetGrades;
    }

    public void setTargetGrades(String targetGrades) {
        this.targetGrades = targetGrades;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public String getJarChecksum() {
        return jarChecksum;
    }

    public void setJarChecksum(String jarChecksum) {
        this.jarChecksum = jarChecksum;
    }

    public LocalDateTime getInstalledAt() {
        return installedAt;
    }

    public void setInstalledAt(LocalDateTime installedAt) {
        this.installedAt = installedAt;
    }

    public LocalDateTime getLastPlayedAt() {
        return lastPlayedAt;
    }

    public void setLastPlayedAt(LocalDateTime lastPlayedAt) {
        this.lastPlayedAt = lastPlayedAt;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    @Override
    public String toString() {
        return "InstalledGame{" +
                "gameId='" + gameId + '\'' +
                ", gameName='" + gameName + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
