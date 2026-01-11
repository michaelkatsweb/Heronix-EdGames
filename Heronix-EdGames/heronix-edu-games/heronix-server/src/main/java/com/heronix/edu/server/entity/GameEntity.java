package com.heronix.edu.server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an educational game available for download
 */
@Entity
@Table(name = "games")
public class GameEntity {

    @Id
    @Column(length = 255)
    private String gameId;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(length = 50)
    private String version;

    @Column(length = 100)
    private String subject;

    @Column(length = 500)
    private String targetGrades; // JSON array stored as string

    @Column(nullable = false)
    private String jarFileName;

    private Long fileSizeBytes;

    @Column(length = 64)
    private String checksum; // SHA-256

    private LocalDateTime uploadedAt;

    private boolean active = true;

    // Constructors
    public GameEntity() {
    }

    public GameEntity(String gameId, String name, String jarFileName) {
        this.gameId = gameId;
        this.name = name;
        this.jarFileName = jarFileName;
        this.uploadedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getJarFileName() {
        return jarFileName;
    }

    public void setJarFileName(String jarFileName) {
        this.jarFileName = jarFileName;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
