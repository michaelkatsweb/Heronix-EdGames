package com.heronix.edu.client.multiplayer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * STOMP-based WebSocket client for multiplayer game communication.
 * Uses Spring's WebSocketStompClient for proper STOMP protocol support.
 *
 * This client:
 * - Connects to the server using STOMP over WebSocket
 * - Handles automatic reconnection on disconnect
 * - Supports session-code based authentication (no JWT required)
 * - Uses SockJS fallback for browser compatibility
 */
public class StompGameClient {
    private static final Logger logger = LoggerFactory.getLogger(StompGameClient.class);

    private final String serverUrl;
    private final ObjectMapper objectMapper;
    private WebSocketStompClient stompClient;
    private StompSession session;

    private String playerId;
    private String sessionCode;
    private String studentId;
    private String studentName;

    // Event handlers
    private Consumer<Boolean> connectionStatusHandler;
    private final ConcurrentHashMap<String, Consumer<Map<String, Object>>> messageHandlers = new ConcurrentHashMap<>();

    // Connection state
    private volatile boolean connected = false;
    private volatile boolean connecting = false;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> reconnectTask;
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    private static final long RECONNECT_DELAY_MS = 3000;

    public StompGameClient(String serverUrl) {
        this.serverUrl = serverUrl;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        initializeClient();
    }

    private void initializeClient() {
        // Create WebSocket client with SockJS fallback
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));

        SockJsClient sockJsClient = new SockJsClient(transports);

        stompClient = new WebSocketStompClient(sockJsClient);

        // Configure message converter
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        stompClient.setMessageConverter(converter);

        // Create and configure TaskScheduler for heartbeats
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("stomp-heartbeat-");
        taskScheduler.setDaemon(true);
        taskScheduler.initialize();
        stompClient.setTaskScheduler(taskScheduler);

        // Set default heartbeat (10 seconds)
        stompClient.setDefaultHeartbeat(new long[]{10000, 10000});

        // Set receive buffer size limit (128KB)
        stompClient.setInboundMessageSizeLimit(128 * 1024);
    }

    /**
     * Connect to the game server.
     */
    public CompletableFuture<Boolean> connect() {
        if (connected || connecting) {
            return CompletableFuture.completedFuture(connected);
        }

        connecting = true;
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try {
            WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
            StompHeaders connectHeaders = new StompHeaders();

            // Add session info to headers
            if (sessionCode != null) {
                connectHeaders.add("sessionCode", sessionCode);
            }
            if (studentId != null) {
                connectHeaders.add("studentId", studentId);
            }
            if (studentName != null) {
                connectHeaders.add("studentName", studentName);
            }

            String wsUrl = serverUrl + "/ws/game";
            logger.info("Connecting to WebSocket server: {}", wsUrl);

            stompClient.connectAsync(wsUrl, headers, connectHeaders, new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    StompGameClient.this.session = session;
                    connected = true;
                    connecting = false;
                    reconnectAttempts = 0;

                    // Extract player ID from headers if provided
                    String assignedPlayerId = connectedHeaders.getFirst("playerId");
                    if (assignedPlayerId != null) {
                        playerId = assignedPlayerId;
                    }

                    logger.info("Connected to game server. SessionId: {}", session.getSessionId());

                    if (connectionStatusHandler != null) {
                        connectionStatusHandler.accept(true);
                    }

                    future.complete(true);
                }

                @Override
                public void handleException(StompSession session, StompCommand command,
                                            StompHeaders headers, byte[] payload, Throwable exception) {
                    logger.error("STOMP error: command={}, exception={}", command, exception.getMessage());
                }

                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    logger.error("Transport error: {}", exception.getMessage());
                    handleDisconnect();
                    if (!future.isDone()) {
                        future.complete(false);
                    }
                }
            });

        } catch (Exception e) {
            logger.error("Failed to connect to WebSocket server", e);
            connecting = false;
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * Disconnect from the server.
     */
    public void disconnect() {
        cancelReconnect();
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        connected = false;
        connecting = false;
        logger.info("Disconnected from game server");
    }

    private void handleDisconnect() {
        connected = false;
        connecting = false;

        if (connectionStatusHandler != null) {
            connectionStatusHandler.accept(false);
        }

        // Attempt reconnection
        scheduleReconnect();
    }

    private void scheduleReconnect() {
        if (reconnectTask != null && !reconnectTask.isDone()) {
            return; // Already scheduled
        }

        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            logger.warn("Max reconnect attempts reached. Giving up.");
            return;
        }

        reconnectAttempts++;
        long delay = RECONNECT_DELAY_MS * reconnectAttempts;

        logger.info("Scheduling reconnect attempt {} in {}ms", reconnectAttempts, delay);

        reconnectTask = scheduler.schedule(() -> {
            logger.info("Attempting reconnection...");
            connect().thenAccept(success -> {
                if (success) {
                    logger.info("Reconnected successfully");
                    // Re-subscribe to game session if we had one
                    if (sessionCode != null) {
                        subscribeToSession(sessionCode);
                    }
                }
            });
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void cancelReconnect() {
        if (reconnectTask != null) {
            reconnectTask.cancel(false);
            reconnectTask = null;
        }
        reconnectAttempts = 0;
    }

    /**
     * Subscribe to a game session's topic.
     */
    public void subscribeToSession(String sessionCode) {
        if (!connected || session == null) {
            logger.warn("Cannot subscribe - not connected");
            return;
        }

        this.sessionCode = sessionCode;

        // Subscribe to session broadcast topic
        String sessionTopic = "/topic/session/" + sessionCode;
        session.subscribe(sessionTopic, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void handleFrame(StompHeaders headers, Object payload) {
                Map<String, Object> message = (Map<String, Object>) payload;
                handleMessage(message);
            }
        });
        logger.info("Subscribed to session topic: {}", sessionTopic);

        // Subscribe to user-specific queues
        subscribeToUserQueue("/user/queue/joined", "JOIN_RESPONSE");
        subscribeToUserQueue("/user/queue/question", "QUESTION");
        subscribeToUserQueue("/user/queue/answer-result", "ANSWER_RESULT");
        subscribeToUserQueue("/user/queue/hack-result", "HACK_RESULT");
        subscribeToUserQueue("/user/queue/hacked", "YOU_WERE_HACKED");
        subscribeToUserQueue("/user/queue/leaderboard", "LEADERBOARD");
        subscribeToUserQueue("/user/queue/session-info", "SESSION_INFO");
    }

    private void subscribeToUserQueue(String destination, String messageType) {
        session.subscribe(destination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void handleFrame(StompHeaders headers, Object payload) {
                Map<String, Object> message = (Map<String, Object>) payload;
                message.put("type", messageType);
                handleMessage(message);
            }
        });
        logger.debug("Subscribed to user queue: {}", destination);
    }

    private void handleMessage(Map<String, Object> message) {
        String type = (String) message.get("type");

        if (type != null) {
            Consumer<Map<String, Object>> handler = messageHandlers.get(type);
            if (handler != null) {
                try {
                    handler.accept(message);
                } catch (Exception e) {
                    logger.error("Error handling message type: {}", type, e);
                }
            } else {
                logger.debug("No handler for message type: {}", type);
            }
        }
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
     * Join a game session.
     */
    public void joinSession(String sessionCode, String studentId, String studentName,
                            String secretCode, String avatarId) {
        if (!connected || session == null) {
            logger.warn("Cannot join session - not connected");
            return;
        }

        this.sessionCode = sessionCode;
        this.studentId = studentId;
        this.studentName = studentName;

        Map<String, Object> request = Map.of(
            "studentId", studentId,
            "studentName", studentName,
            "secretCode", secretCode,
            "avatarId", avatarId != null ? avatarId : "ROOKIE_ROBOT"
        );

        session.send("/app/session/" + sessionCode + "/join", request);
        logger.info("Join request sent for session: {}", sessionCode);

        // Subscribe to session topics
        subscribeToSession(sessionCode);
    }

    /**
     * Submit an answer.
     */
    public void submitAnswer(String questionId, String answer) {
        if (!connected || session == null || sessionCode == null) {
            logger.warn("Cannot submit answer - not connected or no session");
            return;
        }

        Map<String, Object> request = Map.of(
            "questionId", questionId,
            "answer", answer
        );

        session.send("/app/session/" + sessionCode + "/answer", request);
        logger.debug("Answer submitted for question: {}", questionId);
    }

    /**
     * Select a reward after correct answer.
     */
    public void selectReward(String rewardType) {
        if (!connected || session == null || sessionCode == null) {
            return;
        }

        Map<String, Object> request = Map.of("rewardType", rewardType);
        session.send("/app/session/" + sessionCode + "/reward", request);
        logger.debug("Reward selected: {}", rewardType);
    }

    /**
     * Attempt to hack another player.
     */
    public void attemptHack(String targetPlayerId, String guessedCode) {
        if (!connected || session == null || sessionCode == null) {
            return;
        }

        Map<String, Object> request = Map.of(
            "targetPlayerId", targetPlayerId,
            "guessedCode", guessedCode
        );

        session.send("/app/session/" + sessionCode + "/hack", request);
        logger.debug("Hack attempt sent for target: {}", targetPlayerId);
    }

    /**
     * Request current leaderboard.
     */
    public void requestLeaderboard() {
        if (!connected || session == null || sessionCode == null) {
            return;
        }

        session.send("/app/session/" + sessionCode + "/leaderboard", Map.of());
    }

    /**
     * Request session info.
     */
    public void requestSessionInfo() {
        if (!connected || session == null || sessionCode == null) {
            return;
        }

        session.send("/app/session/" + sessionCode + "/info", Map.of());
    }

    /**
     * Request next question.
     */
    public void requestQuestion() {
        if (!connected || session == null || sessionCode == null) {
            return;
        }

        session.send("/app/session/" + sessionCode + "/question", Map.of());
    }

    /**
     * Submit challenge answer (when being hacked).
     */
    public void submitChallengeAnswer(String answer) {
        if (!connected || session == null || sessionCode == null) {
            return;
        }

        Map<String, Object> request = Map.of("answer", answer);
        session.send("/app/session/" + sessionCode + "/challenge-answer", request);
    }

    /**
     * Complete mini challenge (when being hacked - timeout).
     */
    public void completeMiniChallenge(boolean success) {
        if (!connected || session == null || sessionCode == null) {
            return;
        }

        Map<String, Object> request = Map.of("success", success);
        session.send("/app/session/" + sessionCode + "/challenge-complete", request);
    }

    // Getters

    public boolean isConnected() {
        return connected && session != null && session.isConnected();
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getSessionCode() {
        return sessionCode;
    }

    /**
     * Cleanup resources.
     */
    public void shutdown() {
        disconnect();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
