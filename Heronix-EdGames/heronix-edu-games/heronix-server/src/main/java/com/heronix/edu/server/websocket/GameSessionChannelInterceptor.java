package com.heronix.edu.server.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * STOMP channel interceptor for game session authentication and connection tracking.
 *
 * This interceptor:
 * - Assigns a unique player ID to each connection (no JWT required)
 * - Tracks active connections for monitoring
 * - Handles session code validation at connect time
 * - Logs connection/disconnection events for debugging
 */
@Component
public class GameSessionChannelInterceptor implements ChannelInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(GameSessionChannelInterceptor.class);

    // Track active connections for monitoring
    private final Map<String, ConnectionInfo> activeConnections = new ConcurrentHashMap<>();
    private final AtomicInteger connectionCounter = new AtomicInteger(0);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command == null) {
            return message;
        }

        switch (command) {
            case CONNECT -> handleConnect(accessor);
            case DISCONNECT -> handleDisconnect(accessor);
            case SUBSCRIBE -> handleSubscribe(accessor);
            case SEND -> handleSend(accessor);
            default -> { }
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        // Generate unique player ID for this connection
        String playerId = generatePlayerId();

        // Get session code from headers if provided
        String sessionCode = accessor.getFirstNativeHeader("sessionCode");
        String studentName = accessor.getFirstNativeHeader("studentName");
        String studentId = accessor.getFirstNativeHeader("studentId");

        // Create a simple principal for the connection
        GamePlayerPrincipal principal = new GamePlayerPrincipal(playerId, sessionCode, studentName, studentId);
        accessor.setUser(principal);

        // Track connection
        String sessionId = accessor.getSessionId();
        ConnectionInfo info = new ConnectionInfo(playerId, sessionCode, studentName, System.currentTimeMillis());
        activeConnections.put(sessionId, info);

        int totalConnections = connectionCounter.incrementAndGet();
        logger.info("WebSocket CONNECT: playerId={}, sessionCode={}, studentName={}, totalConnections={}",
            playerId, sessionCode, studentName, totalConnections);
    }

    private void handleDisconnect(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        ConnectionInfo info = activeConnections.remove(sessionId);

        int totalConnections = connectionCounter.decrementAndGet();

        if (info != null) {
            long duration = System.currentTimeMillis() - info.connectedAt;
            logger.info("WebSocket DISCONNECT: playerId={}, sessionCode={}, duration={}ms, totalConnections={}",
                info.playerId, info.sessionCode, duration, totalConnections);
        } else {
            logger.debug("WebSocket DISCONNECT: sessionId={}, totalConnections={}", sessionId, totalConnections);
        }
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        Principal user = accessor.getUser();
        String playerId = user != null ? user.getName() : "unknown";

        logger.debug("WebSocket SUBSCRIBE: playerId={}, destination={}", playerId, destination);
    }

    private void handleSend(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        Principal user = accessor.getUser();
        String playerId = user != null ? user.getName() : "unknown";

        logger.debug("WebSocket SEND: playerId={}, destination={}", playerId, destination);
    }

    private String generatePlayerId() {
        return "player-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Get current number of active WebSocket connections.
     */
    public int getActiveConnectionCount() {
        return connectionCounter.get();
    }

    /**
     * Get all active connection info (for monitoring/debugging).
     */
    public Map<String, ConnectionInfo> getActiveConnections() {
        return Map.copyOf(activeConnections);
    }

    /**
     * Connection tracking info.
     */
    public record ConnectionInfo(String playerId, String sessionCode, String studentName, long connectedAt) {}

    /**
     * Simple Principal implementation for game players.
     * This allows us to identify players without requiring JWT authentication.
     */
    public static class GamePlayerPrincipal implements Principal {
        private final String playerId;
        private final String sessionCode;
        private final String studentName;
        private final String studentId;

        public GamePlayerPrincipal(String playerId, String sessionCode, String studentName, String studentId) {
            this.playerId = playerId;
            this.sessionCode = sessionCode;
            this.studentName = studentName;
            this.studentId = studentId;
        }

        @Override
        public String getName() {
            return playerId;
        }

        public String getPlayerId() {
            return playerId;
        }

        public String getSessionCode() {
            return sessionCode;
        }

        public String getStudentName() {
            return studentName;
        }

        public String getStudentId() {
            return studentId;
        }

        @Override
        public String toString() {
            return "GamePlayer[" + playerId + ", " + studentName + ", session=" + sessionCode + "]";
        }
    }
}
