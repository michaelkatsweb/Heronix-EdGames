package com.heronix.edu.server.dto.game;

import java.util.List;

/**
 * Response after attempting to join a game session.
 */
public class JoinSessionResponse {
    private boolean success;
    private String message;
    private String playerId;
    private String sessionId;
    private String gameType;
    private List<PlayerDto> otherPlayers;  // List of other players in the session

    public JoinSessionResponse() {}

    public static JoinSessionResponse success(String playerId, String sessionId, String gameType, List<PlayerDto> otherPlayers) {
        JoinSessionResponse response = new JoinSessionResponse();
        response.success = true;
        response.playerId = playerId;
        response.sessionId = sessionId;
        response.gameType = gameType;
        response.otherPlayers = otherPlayers;
        return response;
    }

    public static JoinSessionResponse error(String message) {
        JoinSessionResponse response = new JoinSessionResponse();
        response.success = false;
        response.message = message;
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public List<PlayerDto> getOtherPlayers() {
        return otherPlayers;
    }

    public void setOtherPlayers(List<PlayerDto> otherPlayers) {
        this.otherPlayers = otherPlayers;
    }
}
