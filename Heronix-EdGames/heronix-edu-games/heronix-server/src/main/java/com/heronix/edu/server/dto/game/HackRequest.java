package com.heronix.edu.server.dto.game;

/**
 * Request from a player attempting to hack another player.
 */
public class HackRequest {
    private String targetPlayerId;  // Player to hack
    private String guessedCode;     // The secret code guess

    public HackRequest() {}

    public HackRequest(String targetPlayerId, String guessedCode) {
        this.targetPlayerId = targetPlayerId;
        this.guessedCode = guessedCode;
    }

    public String getTargetPlayerId() {
        return targetPlayerId;
    }

    public void setTargetPlayerId(String targetPlayerId) {
        this.targetPlayerId = targetPlayerId;
    }

    public String getGuessedCode() {
        return guessedCode;
    }

    public void setGuessedCode(String guessedCode) {
        this.guessedCode = guessedCode;
    }
}
