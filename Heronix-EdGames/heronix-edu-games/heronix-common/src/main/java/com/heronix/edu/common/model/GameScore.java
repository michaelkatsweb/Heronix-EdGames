package com.heronix.edu.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a game score/result for a student.
 * This is an education record under FERPA.
 */
public class GameScore {
    
    /**
     * Unique identifier for this score record
     */
    private String scoreId;
    
    /**
     * Student who earned this score
     */
    private String studentId;
    
    /**
     * Game that was played
     */
    private String gameId;
    
    /**
     * Numeric score (0-100)
     */
    private int score;
    
    /**
     * Maximum possible score
     */
    private int maxScore;
    
    /**
     * Time taken to complete (in seconds)
     */
    private int timeSeconds;
    
    /**
     * Number of correct answers
     */
    private int correctAnswers;
    
    /**
     * Number of incorrect answers
     */
    private int incorrectAnswers;
    
    /**
     * Completion percentage (0-100)
     */
    private int completionPercentage;
    
    /**
     * Whether the game was completed
     */
    private boolean completed;
    
    /**
     * Difficulty level played
     */
    private String difficultyLevel;
    
    /**
     * When this score was achieved
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime playedAt;
    
    /**
     * Device ID where game was played
     */
    private String deviceId;
    
    /**
     * Whether this score has been synced to server
     */
    private boolean synced;
    
    /**
     * When this score was synced
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime syncedAt;
    
    /**
     * Additional metadata (JSON string)
     */
    private String metadata;
    
    // Constructors
    public GameScore() {
        this.scoreId = UUID.randomUUID().toString();
        this.playedAt = LocalDateTime.now();
        this.synced = false;
        this.completed = false;
        this.maxScore = 100;
        this.completionPercentage = 0;
    }
    
    public GameScore(String studentId, String gameId, int score) {
        this();
        this.studentId = studentId;
        this.gameId = gameId;
        this.score = score;
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
        this.score = Math.max(0, Math.min(score, maxScore));
    }
    
    public int getMaxScore() {
        return maxScore;
    }
    
    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }
    
    public int getTimeSeconds() {
        return timeSeconds;
    }
    
    public void setTimeSeconds(int timeSeconds) {
        this.timeSeconds = timeSeconds;
    }
    
    public int getCorrectAnswers() {
        return correctAnswers;
    }
    
    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }
    
    public int getIncorrectAnswers() {
        return incorrectAnswers;
    }
    
    public void setIncorrectAnswers(int incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
    }
    
    public int getCompletionPercentage() {
        return completionPercentage;
    }
    
    public void setCompletionPercentage(int completionPercentage) {
        this.completionPercentage = Math.max(0, Math.min(completionPercentage, 100));
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
        if (synced && this.syncedAt == null) {
            this.syncedAt = LocalDateTime.now();
        }
    }
    
    public LocalDateTime getSyncedAt() {
        return syncedAt;
    }
    
    public void setSyncedAt(LocalDateTime syncedAt) {
        this.syncedAt = syncedAt;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    /**
     * Calculate accuracy percentage
     */
    public double getAccuracy() {
        int total = correctAnswers + incorrectAnswers;
        if (total == 0) return 0.0;
        return (double) correctAnswers / total * 100.0;
    }
    
    /**
     * Get letter grade based on score
     */
    public String getLetterGrade() {
        double percentage = (double) score / maxScore * 100;
        if (percentage >= 90) return "A";
        if (percentage >= 80) return "B";
        if (percentage >= 70) return "C";
        if (percentage >= 60) return "D";
        return "F";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameScore gameScore = (GameScore) o;
        return Objects.equals(scoreId, gameScore.scoreId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(scoreId);
    }
    
    @Override
    public String toString() {
        return "GameScore{" +
                "scoreId='" + scoreId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", gameId='" + gameId + '\'' +
                ", score=" + score + "/" + maxScore +
                ", accuracy=" + String.format("%.1f%%", getAccuracy()) +
                ", completed=" + completed +
                ", synced=" + synced +
                '}';
    }
}
