package com.heronix.edu.server.dto.game;

import com.heronix.edu.server.entity.GameSessionStatus;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO representing a game session for API responses.
 */
public class GameSessionDto {
    private String sessionId;
    private String sessionCode;  // The 6-char join code
    private String teacherId;
    private String gameType;
    private String questionSetId;
    private String questionSetName;
    private GameSessionStatus status;
    private Integer timeLimitSeconds;
    private Integer targetCredits;
    private Integer playerCount;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private List<PlayerDto> players;

    public GameSessionDto() {}

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionCode() {
        return sessionCode;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getQuestionSetId() {
        return questionSetId;
    }

    public void setQuestionSetId(String questionSetId) {
        this.questionSetId = questionSetId;
    }

    public String getQuestionSetName() {
        return questionSetName;
    }

    public void setQuestionSetName(String questionSetName) {
        this.questionSetName = questionSetName;
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

    public Integer getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(Integer playerCount) {
        this.playerCount = playerCount;
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

    public List<PlayerDto> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerDto> players) {
        this.players = players;
    }
}
