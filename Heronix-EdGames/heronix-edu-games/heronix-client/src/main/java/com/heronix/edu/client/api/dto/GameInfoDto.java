package com.heronix.edu.client.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for game information from server
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameInfoDto {

    private String gameId;
    private String name;
    private String description;
    private String version;
    private String subject;
    private List<String> targetGrades;
    private Long fileSizeBytes;
    private String checksum;
    private LocalDateTime uploadedAt;

    // Constructors
    public GameInfoDto() {
    }

    public GameInfoDto(String gameId, String name, String description) {
        this.gameId = gameId;
        this.name = name;
        this.description = description;
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

    public List<String> getTargetGrades() {
        return targetGrades;
    }

    public void setTargetGrades(List<String> targetGrades) {
        this.targetGrades = targetGrades;
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

    /**
     * Format file size for display
     */
    public String getFormattedFileSize() {
        if (fileSizeBytes == null) return "Unknown";

        double kb = fileSizeBytes / 1024.0;
        double mb = kb / 1024.0;

        if (mb >= 1) {
            return String.format("%.1f MB", mb);
        } else {
            return String.format("%.0f KB", kb);
        }
    }

    /**
     * Get formatted grade range
     */
    public String getGradeRange() {
        if (targetGrades == null || targetGrades.isEmpty()) {
            return "All Grades";
        }
        if (targetGrades.size() == 1) {
            return "Grade " + targetGrades.get(0);
        }
        return "Grades " + targetGrades.get(0) + "-" + targetGrades.get(targetGrades.size() - 1);
    }
}
