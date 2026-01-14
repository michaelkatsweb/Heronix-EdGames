package com.heronix.edu.server.dto.game;

/**
 * Request to create a new multiplayer game session.
 * Sent by teacher via WebSocket.
 */
public class CreateSessionRequest {
    private String questionSetId;
    private String gameType;  // CODE_BREAKER, etc.
    private Integer timeLimitMinutes;
    private Integer targetCredits;

    public CreateSessionRequest() {}

    public CreateSessionRequest(String questionSetId, String gameType, Integer timeLimitMinutes) {
        this.questionSetId = questionSetId;
        this.gameType = gameType;
        this.timeLimitMinutes = timeLimitMinutes;
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

    public Integer getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public void setTimeLimitMinutes(Integer timeLimitMinutes) {
        this.timeLimitMinutes = timeLimitMinutes;
    }

    public Integer getTargetCredits() {
        return targetCredits;
    }

    public void setTargetCredits(Integer targetCredits) {
        this.targetCredits = targetCredits;
    }
}
