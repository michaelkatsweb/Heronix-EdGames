package com.heronix.edu.server.dto.game;

import java.time.LocalDateTime;

/**
 * Generic game event sent via WebSocket.
 * Used for real-time updates to teachers and players.
 */
public class GameEvent {
    private String eventType;
    private String sessionId;
    private String playerId;
    private String playerName;
    private Object data;
    private LocalDateTime timestamp;

    public GameEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public GameEvent(String eventType, String sessionId) {
        this();
        this.eventType = eventType;
        this.sessionId = sessionId;
    }

    // Event type constants
    public static final String PLAYER_JOINED = "PLAYER_JOINED";
    public static final String PLAYER_LEFT = "PLAYER_LEFT";
    public static final String GAME_STARTED = "GAME_STARTED";
    public static final String GAME_PAUSED = "GAME_PAUSED";
    public static final String GAME_RESUMED = "GAME_RESUMED";
    public static final String GAME_ENDED = "GAME_ENDED";
    public static final String QUESTION_SENT = "QUESTION_SENT";
    public static final String ANSWER_CORRECT = "ANSWER_CORRECT";
    public static final String ANSWER_INCORRECT = "ANSWER_INCORRECT";
    public static final String HACK_SUCCESS = "HACK_SUCCESS";
    public static final String HACK_FAILED = "HACK_FAILED";
    public static final String PLAYER_HACKED = "PLAYER_HACKED";
    public static final String LEADERBOARD_UPDATE = "LEADERBOARD_UPDATE";
    public static final String TIME_WARNING = "TIME_WARNING";
    public static final String CREDITS_EARNED = "CREDITS_EARNED";

    // Factory methods for common events
    public static GameEvent playerJoined(String sessionId, String playerId, String playerName) {
        GameEvent event = new GameEvent(PLAYER_JOINED, sessionId);
        event.playerId = playerId;
        event.playerName = playerName;
        return event;
    }

    public static GameEvent gameStarted(String sessionId, int timeLimitSeconds) {
        GameEvent event = new GameEvent(GAME_STARTED, sessionId);
        event.data = new GameStartData(timeLimitSeconds);
        return event;
    }

    public static GameEvent gameEnded(String sessionId, Object finalResults) {
        GameEvent event = new GameEvent(GAME_ENDED, sessionId);
        event.data = finalResults;
        return event;
    }

    public static GameEvent hackSuccess(String sessionId, String hackerId, String hackerName,
                                        String targetName, int creditsStolen) {
        GameEvent event = new GameEvent(HACK_SUCCESS, sessionId);
        event.playerId = hackerId;
        event.playerName = hackerName;
        event.data = new HackEventData(targetName, creditsStolen);
        return event;
    }

    // Getters and Setters
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // Inner classes for event data
    public static class GameStartData {
        public int timeLimitSeconds;

        public GameStartData(int timeLimitSeconds) {
            this.timeLimitSeconds = timeLimitSeconds;
        }
    }

    public static class HackEventData {
        public String targetName;
        public int creditsStolen;

        public HackEventData(String targetName, int creditsStolen) {
            this.targetName = targetName;
            this.creditsStolen = creditsStolen;
        }
    }
}
