package com.heronix.edu.server.entity;

import com.heronix.edu.common.model.GameScore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entity for GameScore data.
 * Maps to the 'game_scores' table in the database.
 * This is an education record under FERPA - access must be logged.
 */
@Entity
@Table(name = "game_scores", indexes = {
    @Index(name = "idx_score_id", columnList = "score_id", unique = true),
    @Index(name = "idx_student_id", columnList = "student_id"),
    @Index(name = "idx_game_id", columnList = "game_id"),
    @Index(name = "idx_played_at", columnList = "played_at"),
    @Index(name = "idx_synced", columnList = "synced")
})
public class GameScoreEntity {

    @Id
    @Column(name = "score_id", nullable = false, length = 100)
    private String scoreId;

    @Column(name = "student_id", nullable = false, length = 50)
    private String studentId;

    @Column(name = "game_id", nullable = false, length = 50)
    private String gameId;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "max_score", nullable = false)
    private Integer maxScore = 100;

    @Column(name = "time_seconds")
    private Integer timeSeconds;

    @Column(name = "correct_answers")
    private Integer correctAnswers;

    @Column(name = "incorrect_answers")
    private Integer incorrectAnswers;

    @Column(name = "completion_percentage")
    private Integer completionPercentage;

    @Column(name = "completed", nullable = false)
    private Boolean completed = false;

    @Column(name = "difficulty_level", length = 20)
    private String difficultyLevel;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "synced", nullable = false)
    private Boolean synced = false;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    @Lob
    @Column(name = "metadata", columnDefinition = "CLOB")
    private String metadata;

    // Constructors
    public GameScoreEntity() {
    }

    public GameScoreEntity(String scoreId, String studentId, String gameId, int score) {
        this.scoreId = scoreId;
        this.studentId = studentId;
        this.gameId = gameId;
        this.score = score;
        this.maxScore = 100;
        this.playedAt = LocalDateTime.now();
        this.completed = false;
        this.synced = false;
    }

    /**
     * Convert GameScore domain model to GameScoreEntity
     */
    public static GameScoreEntity fromModel(GameScore score) {
        GameScoreEntity entity = new GameScoreEntity();
        entity.setScoreId(score.getScoreId());
        entity.setStudentId(score.getStudentId());
        entity.setGameId(score.getGameId());
        entity.setScore(score.getScore());
        entity.setMaxScore(score.getMaxScore());
        entity.setTimeSeconds(score.getTimeSeconds());
        entity.setCorrectAnswers(score.getCorrectAnswers());
        entity.setIncorrectAnswers(score.getIncorrectAnswers());
        entity.setCompletionPercentage(score.getCompletionPercentage());
        entity.setCompleted(score.isCompleted());
        entity.setDifficultyLevel(score.getDifficultyLevel());
        entity.setPlayedAt(score.getPlayedAt());
        entity.setDeviceId(score.getDeviceId());
        entity.setSynced(score.isSynced());
        entity.setSyncedAt(score.getSyncedAt());
        entity.setMetadata(score.getMetadata());
        return entity;
    }

    /**
     * Convert GameScoreEntity to GameScore domain model
     */
    public GameScore toModel() {
        GameScore score = new GameScore();
        score.setScoreId(this.scoreId);
        score.setStudentId(this.studentId);
        score.setGameId(this.gameId);
        score.setScore(this.score);
        score.setMaxScore(this.maxScore);
        score.setTimeSeconds(this.timeSeconds);
        score.setCorrectAnswers(this.correctAnswers);
        score.setIncorrectAnswers(this.incorrectAnswers);
        score.setCompletionPercentage(this.completionPercentage);
        score.setCompleted(this.completed);
        score.setDifficultyLevel(this.difficultyLevel);
        score.setPlayedAt(this.playedAt);
        score.setDeviceId(this.deviceId);
        score.setSynced(this.synced);
        score.setSyncedAt(this.syncedAt);
        score.setMetadata(this.metadata);
        return score;
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

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
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

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
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

    public Boolean getSynced() {
        return synced;
    }

    public void setSynced(Boolean synced) {
        this.synced = synced;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameScoreEntity that = (GameScoreEntity) o;
        return Objects.equals(scoreId, that.scoreId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scoreId);
    }

    @Override
    public String toString() {
        return "GameScoreEntity{" +
                "scoreId='" + scoreId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", gameId='" + gameId + '\'' +
                ", score=" + score + "/" + maxScore +
                ", completed=" + completed +
                ", synced=" + synced +
                '}';
    }
}
