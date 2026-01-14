package com.heronix.edu.server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a multiplayer game session (e.g., Code Breaker).
 * Sessions are created by teachers and joined by students.
 */
@Entity
@Table(name = "game_sessions")
public class GameSessionEntity {

    @Id
    @Column(name = "session_id", length = 10)
    private String sessionId;  // 6-char code like "ABC123"

    @Column(name = "teacher_id", nullable = false)
    private String teacherId;

    @Column(name = "question_set_id")
    private String questionSetId;

    @Column(name = "game_type", nullable = false)
    private String gameType;  // CODE_BREAKER, etc.

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GameSessionStatus status = GameSessionStatus.WAITING;

    @Column(name = "time_limit_seconds")
    private Integer timeLimitSeconds;

    @Column(name = "target_credits")
    private Integer targetCredits;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GamePlayerEntity> players = new ArrayList<>();

    // Constructors
    public GameSessionEntity() {
        this.createdAt = LocalDateTime.now();
    }

    public GameSessionEntity(String sessionId, String teacherId, String gameType) {
        this();
        this.sessionId = sessionId;
        this.teacherId = teacherId;
        this.gameType = gameType;
    }

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getQuestionSetId() {
        return questionSetId;
    }

    public void setQuestionSetId(String questionSetId) {
        this.questionSetId = questionSetId;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public GameSessionStatus getStatus() {
        return status;
    }

    public void setStatus(GameSessionStatus status) {
        this.status = status;
    }

    public Integer getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public void setTimeLimitSeconds(Integer timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public Integer getTargetCredits() {
        return targetCredits;
    }

    public void setTargetCredits(Integer targetCredits) {
        this.targetCredits = targetCredits;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public List<GamePlayerEntity> getPlayers() {
        return players;
    }

    public void setPlayers(List<GamePlayerEntity> players) {
        this.players = players;
    }

    public void addPlayer(GamePlayerEntity player) {
        players.add(player);
        player.setSession(this);
    }

    public void removePlayer(GamePlayerEntity player) {
        players.remove(player);
        player.setSession(null);
    }
}
