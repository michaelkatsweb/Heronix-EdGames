package com.heronix.edu.client.multiplayer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StompGameClient.
 * Tests client initialization, message handling, and state management.
 * Note: These tests focus on testable aspects without requiring a real WebSocket server.
 */
class StompGameClientTest {

    private StompGameClient client;

    @BeforeEach
    void setUp() {
        client = new StompGameClient("http://localhost:8081");
    }

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.shutdown();
        }
    }

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("Should create client with valid server URL")
        void shouldCreateClientWithValidServerUrl() {
            assertNotNull(client);
            assertFalse(client.isConnected());
        }

        @Test
        @DisplayName("Should start with null player ID")
        void shouldStartWithNullPlayerId() {
            assertNull(client.getPlayerId());
        }

        @Test
        @DisplayName("Should start with null session code")
        void shouldStartWithNullSessionCode() {
            assertNull(client.getSessionCode());
        }

        @Test
        @DisplayName("Should not be connected initially")
        void shouldNotBeConnectedInitially() {
            assertFalse(client.isConnected());
        }
    }

    @Nested
    @DisplayName("Player ID Management Tests")
    class PlayerIdManagementTests {

        @Test
        @DisplayName("Should set and get player ID")
        void shouldSetAndGetPlayerId() {
            client.setPlayerId("player-123");
            assertEquals("player-123", client.getPlayerId());
        }

        @Test
        @DisplayName("Should allow changing player ID")
        void shouldAllowChangingPlayerId() {
            client.setPlayerId("player-1");
            client.setPlayerId("player-2");
            assertEquals("player-2", client.getPlayerId());
        }

        @Test
        @DisplayName("Should allow null player ID")
        void shouldAllowNullPlayerId() {
            client.setPlayerId("player-123");
            client.setPlayerId(null);
            assertNull(client.getPlayerId());
        }
    }

    @Nested
    @DisplayName("Message Handler Registration Tests")
    class MessageHandlerTests {

        @Test
        @DisplayName("Should register message handler")
        void shouldRegisterMessageHandler() {
            AtomicBoolean handlerCalled = new AtomicBoolean(false);

            client.onMessage("TEST_TYPE", msg -> {
                handlerCalled.set(true);
            });

            // Handler registration should not throw
            assertFalse(handlerCalled.get()); // Not called until message received
        }

        @Test
        @DisplayName("Should support multiple message handlers")
        void shouldSupportMultipleMessageHandlers() {
            AtomicBoolean handler1Called = new AtomicBoolean(false);
            AtomicBoolean handler2Called = new AtomicBoolean(false);

            client.onMessage("TYPE_A", msg -> handler1Called.set(true));
            client.onMessage("TYPE_B", msg -> handler2Called.set(true));

            // Both handlers should be registered without conflict
            assertFalse(handler1Called.get());
            assertFalse(handler2Called.get());
        }

        @Test
        @DisplayName("Should override handler for same message type")
        void shouldOverrideHandlerForSameMessageType() {
            AtomicReference<String> receivedValue = new AtomicReference<>();

            client.onMessage("SAME_TYPE", msg -> receivedValue.set("first"));
            client.onMessage("SAME_TYPE", msg -> receivedValue.set("second"));

            // The last handler should replace the first
            // We can't directly test this without connection, but registration shouldn't throw
            assertNull(receivedValue.get());
        }
    }

    @Nested
    @DisplayName("Connection Status Handler Tests")
    class ConnectionStatusHandlerTests {

        @Test
        @DisplayName("Should set connection status handler")
        void shouldSetConnectionStatusHandler() {
            AtomicBoolean statusReceived = new AtomicBoolean(false);

            client.setConnectionStatusHandler(connected -> {
                statusReceived.set(true);
            });

            // Handler set without throwing
            assertFalse(statusReceived.get()); // Not called until connection state changes
        }

        @Test
        @DisplayName("Should accept null connection status handler")
        void shouldAcceptNullConnectionStatusHandler() {
            client.setConnectionStatusHandler(connected -> {});
            client.setConnectionStatusHandler(null);
            // Should not throw
        }
    }

    @Nested
    @DisplayName("Disconnect Tests")
    class DisconnectTests {

        @Test
        @DisplayName("Should handle disconnect when not connected")
        void shouldHandleDisconnectWhenNotConnected() {
            assertDoesNotThrow(() -> client.disconnect());
            assertFalse(client.isConnected());
        }

        @Test
        @DisplayName("Should be safe to call disconnect multiple times")
        void shouldBeSafeToCallDisconnectMultipleTimes() {
            assertDoesNotThrow(() -> {
                client.disconnect();
                client.disconnect();
                client.disconnect();
            });
        }
    }

    @Nested
    @DisplayName("Shutdown Tests")
    class ShutdownTests {

        @Test
        @DisplayName("Should shutdown cleanly")
        void shouldShutdownCleanly() {
            assertDoesNotThrow(() -> client.shutdown());
        }

        @Test
        @DisplayName("Should be safe to shutdown when not connected")
        void shouldBeSafeToShutdownWhenNotConnected() {
            assertDoesNotThrow(() -> {
                StompGameClient newClient = new StompGameClient("http://localhost:8081");
                newClient.shutdown();
            });
        }

        @Test
        @DisplayName("Should be safe to shutdown multiple times")
        void shouldBeSafeToShutdownMultipleTimes() {
            StompGameClient testClient = new StompGameClient("http://localhost:8081");
            assertDoesNotThrow(() -> {
                testClient.shutdown();
                testClient.shutdown();
            });
        }
    }

    @Nested
    @DisplayName("Unconnected Operation Tests")
    class UnconnectedOperationTests {

        @Test
        @DisplayName("Should not throw when submitting answer while disconnected")
        void shouldNotThrowWhenSubmittingAnswerWhileDisconnected() {
            // Should log warning but not throw
            assertDoesNotThrow(() -> client.submitAnswer("q1", "answer"));
        }

        @Test
        @DisplayName("Should not throw when selecting reward while disconnected")
        void shouldNotThrowWhenSelectingRewardWhileDisconnected() {
            assertDoesNotThrow(() -> client.selectReward("CREDITS"));
        }

        @Test
        @DisplayName("Should not throw when attempting hack while disconnected")
        void shouldNotThrowWhenAttemptingHackWhileDisconnected() {
            assertDoesNotThrow(() -> client.attemptHack("target-123", "ALPHA"));
        }

        @Test
        @DisplayName("Should not throw when requesting leaderboard while disconnected")
        void shouldNotThrowWhenRequestingLeaderboardWhileDisconnected() {
            assertDoesNotThrow(() -> client.requestLeaderboard());
        }

        @Test
        @DisplayName("Should not throw when requesting session info while disconnected")
        void shouldNotThrowWhenRequestingSessionInfoWhileDisconnected() {
            assertDoesNotThrow(() -> client.requestSessionInfo());
        }

        @Test
        @DisplayName("Should not throw when requesting question while disconnected")
        void shouldNotThrowWhenRequestingQuestionWhileDisconnected() {
            assertDoesNotThrow(() -> client.requestQuestion());
        }

        @Test
        @DisplayName("Should not throw when submitting challenge answer while disconnected")
        void shouldNotThrowWhenSubmittingChallengeAnswerWhileDisconnected() {
            assertDoesNotThrow(() -> client.submitChallengeAnswer("answer"));
        }

        @Test
        @DisplayName("Should not throw when completing mini challenge while disconnected")
        void shouldNotThrowWhenCompletingMiniChallengeWhileDisconnected() {
            assertDoesNotThrow(() -> client.completeMiniChallenge(true));
        }

        @Test
        @DisplayName("Should not throw when subscribing while disconnected")
        void shouldNotThrowWhenSubscribingWhileDisconnected() {
            assertDoesNotThrow(() -> client.subscribeToSession("ABC123"));
        }

        @Test
        @DisplayName("Should not throw when joining session while disconnected")
        void shouldNotThrowWhenJoiningSessionWhileDisconnected() {
            assertDoesNotThrow(() -> client.joinSession("ABC123", "student-1", "John", "ALPHA", "CLEVER_CAT"));
        }
    }

    @Nested
    @DisplayName("Connect Tests")
    class ConnectTests {

        @Test
        @DisplayName("Should return CompletableFuture from connect")
        void shouldReturnCompletableFutureFromConnect() {
            var future = client.connect();
            assertNotNull(future);
            // Cancel to clean up - we're not actually connecting
            future.cancel(true);
        }

        @Test
        @DisplayName("Should handle connect when already connecting")
        void shouldHandleConnectWhenAlreadyConnecting() {
            var future1 = client.connect();
            var future2 = client.connect();

            // Second call should return same state (already connecting)
            assertNotNull(future1);
            assertNotNull(future2);

            // Cancel both to clean up
            future1.cancel(true);
            future2.cancel(true);
        }
    }

    @Nested
    @DisplayName("URL Configuration Tests")
    class UrlConfigurationTests {

        @Test
        @DisplayName("Should accept localhost URL")
        void shouldAcceptLocalhostUrl() {
            StompGameClient localClient = new StompGameClient("http://localhost:8081");
            assertNotNull(localClient);
            localClient.shutdown();
        }

        @Test
        @DisplayName("Should accept IP address URL")
        void shouldAcceptIpAddressUrl() {
            StompGameClient ipClient = new StompGameClient("http://192.168.1.100:8081");
            assertNotNull(ipClient);
            ipClient.shutdown();
        }

        @Test
        @DisplayName("Should accept HTTPS URL")
        void shouldAcceptHttpsUrl() {
            StompGameClient httpsClient = new StompGameClient("https://game.heronix.edu");
            assertNotNull(httpsClient);
            httpsClient.shutdown();
        }
    }
}
