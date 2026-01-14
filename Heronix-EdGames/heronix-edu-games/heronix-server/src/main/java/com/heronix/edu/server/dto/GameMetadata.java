package com.heronix.edu.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Game metadata loaded from game.json files in the Heronix-games directory.
 * This represents the complete metadata for an educational game.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameMetadata {

    private String gameId;
    private String name;
    private String description;
    private String version;
    private String subject;
    private List<String> targetGrades;
    private List<String> difficulty;
    private String jarFile;
    private String icon;
    private List<String> screenshots;
    private String author;
    private String bundle;
    private List<String> features;
    private List<String> learningObjectives;
    private Integer minPlayTime;
    private Integer maxPlayTime;
    private boolean active = true;

    // Computed fields (set during scanning)
    private Long fileSizeBytes;
    private String checksum;
    private String iconUrl;
    private List<String> screenshotUrls;

    public GameMetadata() {}

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

    public List<String> getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(List<String> difficulty) {
        this.difficulty = difficulty;
    }

    public String getJarFile() {
        return jarFile;
    }

    public void setJarFile(String jarFile) {
        this.jarFile = jarFile;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<String> getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(List<String> screenshots) {
        this.screenshots = screenshots;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBundle() {
        return bundle;
    }

    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public List<String> getLearningObjectives() {
        return learningObjectives;
    }

    public void setLearningObjectives(List<String> learningObjectives) {
        this.learningObjectives = learningObjectives;
    }

    public Integer getMinPlayTime() {
        return minPlayTime;
    }

    public void setMinPlayTime(Integer minPlayTime) {
        this.minPlayTime = minPlayTime;
    }

    public Integer getMaxPlayTime() {
        return maxPlayTime;
    }

    public void setMaxPlayTime(Integer maxPlayTime) {
        this.maxPlayTime = maxPlayTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public List<String> getScreenshotUrls() {
        return screenshotUrls;
    }

    public void setScreenshotUrls(List<String> screenshotUrls) {
        this.screenshotUrls = screenshotUrls;
    }
}
