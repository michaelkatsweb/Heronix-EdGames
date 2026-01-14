package com.heronix.edu.server.websocket;

import com.heronix.edu.server.service.GameSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket event listener for tracking connection lifecycle events.
 * Provides real-time monitoring of active connections and handles
 * cleanup when players disconnect.
 */
@Component
public class WebSocketEventListener {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    // Connection statistics
    private final AtomicInteger totalConnections = new AtomicInteger(0);
    private final AtomicInteger peakConnections = new AtomicInteger(0);
    private final Map<String, SessionInfo> sessionMap = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private GameSessionService gameSessionService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Principal user = accessor.getUser();
        String playerId = user != null ? user.getName() : "anonymous";

        // Track the session
        SessionInfo info = new SessionInfo(playerId, System.currentTimeMillis());
        sessionMap.put(sessionId, info);

        int current = totalConnections.incrementAndGet();
        peakConnections.updateAndGet(peak -> Math.max(peak, current));

        logger.info("WebSocket Connected: sessionId={}, playerId={}, totalConnections={}, peakConnections={}",
            sessionId, playerId, current, peakConnections.get());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Principal user = accessor.getUser();

        SessionInfo info = sessionMap.remove(sessionId);
        int current = totalConnections.decrementAndGet();

        if (info != null) {
            long duration = System.currentTimeMillis() - info.connectedAt;
            logger.info("WebSocket Disconnected: sessionId={}, playerId={}, duration={}ms, totalConnections={}",
                sessionId, info.playerId, duration, current);

            // Notify game session service about player disconnect
            if (user instanceof GameSessionChannelInterceptor.GamePlayerPrincipal gamePrincipal) {
                handlePlayerDisconnect(gamePrincipal);
            }
        } else {
            logger.debug("WebSocket Disconnected: sessionId={}, totalConnections={}", sessionId, current);
        }
    }

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        String sessionId = accessor.getSessionId();

        logger.debug("WebSocket Subscribe: sessionId={}, destination={}", sessionId, destination);
    }

    private void handlePlayerDisconnect(GameSessionChannelInterceptor.GamePlayerPrincipal principal) {
        String sessionCode = principal.getSessionCode();
        String playerId = principal.getPlayerId();

        if (sessionCode != null && gameSessionService != null) {
            try {
                // Mark player as disconnected in game session
                // The GameSessionService can handle reconnection logic
                logger.info("Player {} disconnected from game session {}", playerId, sessionCode);
                // TODO: Implement player disconnect handling in GameSessionService
            } catch (Exception e) {
                logger.error("Error handling player disconnect for {}", playerId, e);
            }
        }
    }

    /**
     * Get current connection count.
     */
    public int getCurrentConnections() {
        return totalConnections.get();
    }

    /**
     * Get peak connection count since server start.
     */
    public int getPeakConnections() {
        return peakConnections.get();
    }

    /**
     * Get all active sessions.
     */
    public Map<String, SessionInfo> getActiveSessions() {
        return Map.copyOf(sessionMap);
    }

    /**
     * Reset statistics (for testing/admin purposes).
     */
    public void resetStatistics() {
        peakConnections.set(totalConnections.get());
    }

    /**
     * Session tracking info.
     */
    public record SessionInfo(String playerId, long connectedAt) {}
}
