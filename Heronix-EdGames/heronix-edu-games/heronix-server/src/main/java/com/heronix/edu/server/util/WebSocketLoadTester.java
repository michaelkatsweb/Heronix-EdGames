package com.heronix.edu.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility for load testing WebSocket connections.
 * Simulates multiple concurrent student connections to test scalability.
 *
 * Usage (from main method or test):
 *   WebSocketLoadTester tester = new WebSocketLoadTester("ws://localhost:8081");
 *   tester.runLoadTest(500, "TEST123"); // 500 connections to session TEST123
 */
public class WebSocketLoadTester {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketLoadTester.class);

    private final String serverUrl;
    private final List<StompSession> activeSessions = new CopyOnWriteArrayList<>();
    private final AtomicInteger successfulConnections = new AtomicInteger(0);
    private final AtomicInteger failedConnections = new AtomicInteger(0);
    private final AtomicInteger messagesReceived = new AtomicInteger(0);

    public WebSocketLoadTester(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    /**
     * Run a load test with specified number of concurrent connections.
     *
     * @param numConnections Number of WebSocket connections to create
     * @param sessionCode Game session code to join
     * @return LoadTestResult with statistics
     */
    public LoadTestResult runLoadTest(int numConnections, String sessionCode) {
        logger.info("Starting load test with {} connections to session {}", numConnections, sessionCode);

        long startTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(numConnections, 100));
        CountDownLatch latch = new CountDownLatch(numConnections);

        for (int i = 0; i < numConnections; i++) {
            final int clientId = i;
            executor.submit(() -> {
                try {
                    createConnection(clientId, sessionCode);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all connections to complete (with timeout)
        try {
            boolean completed = latch.await(120, TimeUnit.SECONDS);
            if (!completed) {
                logger.warn("Load test timed out - not all connections completed");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Load test interrupted", e);
        }

        long duration = System.currentTimeMillis() - startTime;

        executor.shutdown();

        LoadTestResult result = new LoadTestResult(
            numConnections,
            successfulConnections.get(),
            failedConnections.get(),
            duration,
            activeSessions.size()
        );

        logger.info("Load test completed: {}", result);
        return result;
    }

    private void createConnection(int clientId, String sessionCode) {
        try {
            // Create WebSocket client
            List<Transport> transports = new ArrayList<>();
            transports.add(new WebSocketTransport(new StandardWebSocketClient()));
            SockJsClient sockJsClient = new SockJsClient(transports);

            WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());
            stompClient.setDefaultHeartbeat(new long[]{10000, 10000});

            String wsUrl = serverUrl + "/ws/game";

            StompHeaders connectHeaders = new StompHeaders();
            connectHeaders.add("sessionCode", sessionCode);
            connectHeaders.add("studentId", "loadtest-" + clientId);
            connectHeaders.add("studentName", "LoadTestStudent" + clientId);

            CompletableFuture<StompSession> future = stompClient.connectAsync(
                wsUrl, new LoadTestSessionHandler(clientId, sessionCode));

            StompSession session = future.get(30, TimeUnit.SECONDS);

            if (session != null && session.isConnected()) {
                activeSessions.add(session);
                successfulConnections.incrementAndGet();
                logger.debug("Client {} connected successfully", clientId);
            } else {
                failedConnections.incrementAndGet();
                logger.warn("Client {} failed to establish session", clientId);
            }

        } catch (Exception e) {
            failedConnections.incrementAndGet();
            logger.warn("Client {} connection failed: {}", clientId, e.getMessage());
        }
    }

    /**
     * Send test messages from all connected clients.
     */
    public void sendTestMessages(String sessionCode, int messagesPerClient) {
        logger.info("Sending {} messages from each of {} clients", messagesPerClient, activeSessions.size());

        for (StompSession session : activeSessions) {
            try {
                for (int i = 0; i < messagesPerClient; i++) {
                    session.send("/app/session/" + sessionCode + "/leaderboard", Map.of());
                    Thread.sleep(100); // Small delay between messages
                }
            } catch (Exception e) {
                logger.warn("Error sending messages: {}", e.getMessage());
            }
        }
    }

    /**
     * Disconnect all test connections.
     */
    public void disconnectAll() {
        logger.info("Disconnecting {} sessions", activeSessions.size());

        for (StompSession session : activeSessions) {
            try {
                if (session.isConnected()) {
                    session.disconnect();
                }
            } catch (Exception e) {
                logger.debug("Error disconnecting session: {}", e.getMessage());
            }
        }

        activeSessions.clear();
        successfulConnections.set(0);
        failedConnections.set(0);
        messagesReceived.set(0);
    }

    /**
     * Get current statistics.
     */
    public Map<String, Integer> getStats() {
        return Map.of(
            "activeSessions", activeSessions.size(),
            "successfulConnections", successfulConnections.get(),
            "failedConnections", failedConnections.get(),
            "messagesReceived", messagesReceived.get()
        );
    }

    /**
     * STOMP session handler for load testing.
     */
    private class LoadTestSessionHandler extends StompSessionHandlerAdapter {
        private final int clientId;
        private final String sessionCode;

        public LoadTestSessionHandler(int clientId, String sessionCode) {
            this.clientId = clientId;
            this.sessionCode = sessionCode;
        }

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            // Subscribe to session topic
            session.subscribe("/topic/session/" + sessionCode, new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Map.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    messagesReceived.incrementAndGet();
                }
            });

            // Send join request
            Map<String, Object> joinRequest = Map.of(
                "studentId", "loadtest-" + clientId,
                "studentName", "LoadTestStudent" + clientId,
                "secretCode", String.format("%04d", clientId % 10000),
                "avatarId", "ROOKIE_ROBOT"
            );

            session.send("/app/session/" + sessionCode + "/join", joinRequest);
        }

        @Override
        public void handleException(StompSession session, StompCommand command,
                                    StompHeaders headers, byte[] payload, Throwable exception) {
            logger.warn("Client {} STOMP error: {}", clientId, exception.getMessage());
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            logger.warn("Client {} transport error: {}", clientId, exception.getMessage());
        }
    }

    /**
     * Load test results.
     */
    public record LoadTestResult(
        int totalAttempted,
        int successfulConnections,
        int failedConnections,
        long durationMs,
        int activeConnectionsAtEnd
    ) {
        public double successRate() {
            return totalAttempted > 0 ? (successfulConnections * 100.0 / totalAttempted) : 0;
        }

        public double connectionsPerSecond() {
            return durationMs > 0 ? (successfulConnections * 1000.0 / durationMs) : 0;
        }

        @Override
        public String toString() {
            return String.format(
                "LoadTestResult{total=%d, successful=%d, failed=%d, " +
                "successRate=%.1f%%, duration=%dms, connectionsPerSec=%.1f, activeAtEnd=%d}",
                totalAttempted, successfulConnections, failedConnections,
                successRate(), durationMs, connectionsPerSecond(), activeConnectionsAtEnd
            );
        }
    }

    /**
     * Main method for standalone testing.
     */
    public static void main(String[] args) {
        String serverUrl = args.length > 0 ? args[0] : "http://localhost:8081";
        int numConnections = args.length > 1 ? Integer.parseInt(args[1]) : 100;
        String sessionCode = args.length > 2 ? args[2] : "TEST01";

        logger.info("=== WebSocket Load Test ===");
        logger.info("Server: {}", serverUrl);
        logger.info("Connections: {}", numConnections);
        logger.info("Session: {}", sessionCode);

        WebSocketLoadTester tester = new WebSocketLoadTester(serverUrl);

        try {
            // Run load test
            LoadTestResult result = tester.runLoadTest(numConnections, sessionCode);
            logger.info("Result: {}", result);

            // Keep connections alive for 30 seconds
            logger.info("Keeping connections alive for 30 seconds...");
            Thread.sleep(30000);

            // Send some test messages
            logger.info("Sending test messages...");
            tester.sendTestMessages(sessionCode, 5);

            Thread.sleep(5000);
            logger.info("Final stats: {}", tester.getStats());

        } catch (Exception e) {
            logger.error("Load test failed", e);
        } finally {
            tester.disconnectAll();
            logger.info("Load test completed");
        }
    }
}
