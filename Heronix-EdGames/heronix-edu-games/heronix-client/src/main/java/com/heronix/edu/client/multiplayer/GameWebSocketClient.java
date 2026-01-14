package com.heronix.edu.client.multiplayer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * WebSocket client for real-time multiplayer game communication.
 * Handles STOMP-like message routing for game events.
 */
public class GameWebSocketClient extends WebSocketClient {
    private static final Logger logger = LoggerFactory.getLogger(GameWebSocketClient.class);

    private final ObjectMapper objectMapper;
    private final Map<String, Consumer<Map<String, Object>>> messageHandlers = new ConcurrentHashMap<>();
    private Consumer<Boolean> connectionStatusHandler;
    private boolean connected = false;
    private String playerId;

    public GameWebSocketClient(String serverUrl) throws Exception {
        super(new URI(serverUrl));
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        logger.info("WebSocket connected to game server");
        connected = true;
        if (connectionStatusHandler != null) {
            connectionStatusHandler.accept(true);
        }
    }

    @Override
    public void onMessage(String message) {
        try {
            Map<String, Object> msg = objectMapper.readValue(message, new TypeReference<>() {});
            String type = (String) msg.get("type");

            if (type != null && messageHandlers.containsKey(type)) {
                messageHandlers.get(type).accept(msg);
            } else {
                // Try to route by destination
                String destination = (String) msg.get("destination");
                if (destination != null && messageHandlers.containsKey(destination)) {
                    messageHandlers.get(destination).accept(msg);
                } else {
                    logger.debug("Unhandled message type: {}", type);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing WebSocket message", e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info("WebSocket closed: {} - {}", code, reason);
        connected = false;
        if (connectionStatusHandler != null) {
            connectionStatusHandler.accept(false);
        }
    }

    @Override
    public void onError(Exception ex) {
        logger.error("WebSocket error", ex);
    }

    /**
     * Register a handler for a specific message type.
     */
    public void onMessage(String type, Consumer<Map<String, Object>> handler) {
        messageHandlers.put(type, handler);
    }

    /**
     * Set connection status handler.
     */
    public void setConnectionStatusHandler(Consumer<Boolean> handler) {
        this.connectionStatusHandler = handler;
    }

    /**
     * Send a message to the server.
     */
    public void sendMessage(String type, Object payload) {
        try {
            Map<String, Object> message = Map.of(
                "type", type,
                "payload", payload,
                "playerId", playerId != null ? playerId : ""
            );
            String json = objectMapper.writeValueAsString(message);
            send(json);
            logger.debug("Sent message: {}", type);
        } catch (Exception e) {
            logger.error("Error sending message", e);
        }
    }

    /**
     * Join a game session.
     */
    public void joinSession(String sessionCode, String studentId, String studentName,
                            String secretCode, String avatarId) {
        Map<String, Object> request = Map.of(
            "sessionCode", sessionCode,
            "studentId", studentId,
            "studentName", studentName,
            "secretCode", secretCode,
            "avatarId", avatarId
        );
        sendMessage("JOIN_SESSION", request);
    }

    /**
     * Submit an answer.
     */
    public void submitAnswer(String questionId, String answer) {
        Map<String, Object> request = Map.of(
            "questionId", questionId,
            "answer", answer
        );
        sendMessage("SUBMIT_ANSWER", request);
    }

    /**
     * Select a reward after correct answer.
     */
    public void selectReward(String rewardType) {
        Map<String, Object> request = Map.of(
            "rewardType", rewardType
        );
        sendMessage("SELECT_REWARD", request);
    }

    /**
     * Attempt to hack another player.
     */
    public void attemptHack(String targetPlayerId, String guessedCode) {
        Map<String, Object> request = Map.of(
            "targetPlayerId", targetPlayerId,
            "guessedCode", guessedCode
        );
        sendMessage("HACK_ATTEMPT", request);
    }

    /**
     * Complete a mini-challenge after being hacked.
     */
    public void completeMiniChallenge(boolean success) {
        Map<String, Object> request = Map.of(
            "success", success
        );
        sendMessage("CHALLENGE_COMPLETE", request);
    }

    public boolean isConnected() {
        return connected && isOpen();
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerId() {
        return playerId;
    }
}
