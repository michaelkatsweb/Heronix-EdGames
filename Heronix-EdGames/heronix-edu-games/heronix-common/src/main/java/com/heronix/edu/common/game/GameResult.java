package com.heronix.edu.common.game;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Result/outcome of a game session
 */
public class GameResult {
    
    private final String gameId;
    private int score;
    private int maxScore;
    private int correctAnswers;
    private int incorrectAnswers;
    private int timeElapsedSeconds;
    private boolean completed;
    private DifficultyLevel difficultyLevel;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private final Map<String, Object> customMetrics;
    
    public GameResult(String gameId) {
        this.gameId = gameId;
        this.score = 0;
        this.maxScore = 100;
        this.correctAnswers = 0;
        this.incorrectAnswers = 0;
        this.timeElapsedSeconds = 0;
        this.completed = false;
        this.startTime = LocalDateTime.now();
        this.customMetrics = new HashMap<>();
    }
    
    public String getGameId() {
        return gameId;
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
    
    public int getCorrectAnswers() {
        return correctAnswers;
    }
    
    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }
    
    public void incrementCorrectAnswers() {
        this.correctAnswers++;
    }
    
    public int getIncorrectAnswers() {
        return incorrectAnswers;
    }
    
    public void setIncorrectAnswers(int incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
    }
    
    public void incrementIncorrectAnswers() {
        this.incorrectAnswers++;
    }
    
    public int getTimeElapsedSeconds() {
        return timeElapsedSeconds;
    }
    
    public void setTimeElapsedSeconds(int timeElapsedSeconds) {
        this.timeElapsedSeconds = timeElapsedSeconds;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && endTime == null) {
            this.endTime = LocalDateTime.now();
        }
    }
    
    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }
    
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Map<String, Object> getCustomMetrics() {
        return customMetrics;
    }
    
    public void addCustomMetric(String key, Object value) {
        customMetrics.put(key, value);
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
     * Calculate score percentage
     */
    public double getScorePercentage() {
        if (maxScore == 0) return 0.0;
        return (double) score / maxScore * 100.0;
    }
    
    /**
     * Get completion percentage (0-100)
     */
    public int getCompletionPercentage() {
        if (!completed) {
            // Estimate based on questions answered if available
            int totalQuestions = correctAnswers + incorrectAnswers;
            if (totalQuestions > 0 && customMetrics.containsKey("totalQuestions")) {
                int total = (int) customMetrics.get("totalQuestions");
                return Math.min(100, (totalQuestions * 100) / total);
            }
            return 0;
        }
        return 100;
    }
    
    @Override
    public String toString() {
        return "GameResult{" +
                "gameId='" + gameId + '\'' +
                ", score=" + score + "/" + maxScore +
                " (" + String.format("%.1f%%", getScorePercentage()) + ")" +
                ", correct=" + correctAnswers +
                ", incorrect=" + incorrectAnswers +
                ", accuracy=" + String.format("%.1f%%", getAccuracy()) +
                ", time=" + timeElapsedSeconds + "s" +
                ", completed=" + completed +
                '}';
    }
}
