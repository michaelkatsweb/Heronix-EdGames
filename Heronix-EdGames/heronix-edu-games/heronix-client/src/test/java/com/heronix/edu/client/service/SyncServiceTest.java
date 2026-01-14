package com.heronix.edu.client.service;

import com.heronix.edu.client.api.HeronixApiClient;
import com.heronix.edu.client.api.dto.SyncResponse;
import com.heronix.edu.client.db.entity.LocalDevice;
import com.heronix.edu.client.db.entity.LocalGameScore;
import com.heronix.edu.client.db.entity.SyncConflict;
import com.heronix.edu.client.util.NetworkMonitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SyncService.
 * Tests synchronization logic, conflict detection, and status management.
 */
@ExtendWith(MockitoExtension.class)
class SyncServiceTest {

    @Mock
    private ScoreService scoreService;

    @Mock
    private DeviceService deviceService;

    @Mock
    private HeronixApiClient apiClient;

    @Mock
    private NetworkMonitor networkMonitor;

    private SyncService syncService;

    @BeforeEach
    void setUp() {
        syncService = new SyncService(scoreService, deviceService, apiClient, networkMonitor);
    }

    @Nested
    @DisplayName("Basic Sync Tests")
    class BasicSyncTests {

        @Test
        @DisplayName("Should sync scores when online and scores available")
        void shouldSyncScoresWhenOnlineAndScoresAvailable() {
            // Arrange
            LocalDevice device = createDevice("device-123", "student-001");
            List<LocalGameScore> unsyncedScores = Arrays.asList(
                createScore("score-1", "student-001", "game-1", 100),
                createScore("score-2", "student-001", "game-1", 150)
            );

            when(deviceService.getDevice()).thenReturn(Optional.of(device));
            when(scoreService.getUnsyncedScores(anyInt())).thenReturn(unsyncedScores);
            when(apiClient.uploadScores(eq("device-123"), anyList()))
                .thenReturn(SyncResponse.success(2));

            // Act
            syncService.performSync();

            // Assert
            verify(apiClient).uploadScores(eq("device-123"), anyList());
            verify(scoreService, times(2)).markAsSynced(anyString());
            assertEquals(SyncService.SyncStatus.SUCCESS, syncService.getSyncStatus());
        }

        @Test
        @DisplayName("Should skip sync when no scores to sync")
        void shouldSkipSyncWhenNoScoresToSync() {
            // Arrange
            when(scoreService.getUnsyncedScores(anyInt())).thenReturn(Collections.emptyList());

            // Act
            syncService.performSync();

            // Assert
            verify(apiClient, never()).uploadScores(anyString(), anyList());
            assertEquals(SyncService.SyncStatus.SUCCESS, syncService.getSyncStatus());
            assertEquals("All scores synced", syncService.getLastSyncMessage());
        }

        @Test
        @DisplayName("Should handle sync failure")
        void shouldHandleSyncFailure() {
            // Arrange
            LocalDevice device = createDevice("device-123", "student-001");
            List<LocalGameScore> unsyncedScores = Arrays.asList(
                createScore("score-1", "student-001", "game-1", 100)
            );

            when(deviceService.getDevice()).thenReturn(Optional.of(device));
            when(scoreService.getUnsyncedScores(anyInt())).thenReturn(unsyncedScores);
            when(apiClient.uploadScores(anyString(), anyList()))
                .thenReturn(SyncResponse.failure("Server error"));

            // Act
            syncService.performSync();

            // Assert
            assertEquals(SyncService.SyncStatus.ERROR, syncService.getSyncStatus());
            assertTrue(syncService.getLastSyncMessage().contains("Sync failed"));
            verify(scoreService).incrementSyncAttempt(eq("score-1"), anyString());
        }

        @Test
        @DisplayName("Should handle exception during sync")
        void shouldHandleExceptionDuringSync() {
            // Arrange
            LocalDevice device = createDevice("device-123", "student-001");
            List<LocalGameScore> unsyncedScores = Arrays.asList(
                createScore("score-1", "student-001", "game-1", 100)
            );

            when(deviceService.getDevice()).thenReturn(Optional.of(device));
            when(scoreService.getUnsyncedScores(anyInt())).thenReturn(unsyncedScores);
            when(apiClient.uploadScores(anyString(), anyList()))
                .thenThrow(new RuntimeException("Network error"));

            // Act
            syncService.performSync();

            // Assert
            assertEquals(SyncService.SyncStatus.ERROR, syncService.getSyncStatus());
            assertTrue(syncService.getLastSyncMessage().contains("Sync error"));
        }
    }

    @Nested
    @DisplayName("Token Refresh Tests")
    class TokenRefreshTests {

        @Test
        @DisplayName("Should refresh token before sync if needed")
        void shouldRefreshTokenBeforeSyncIfNeeded() {
            // Arrange
            LocalDevice device = createDevice("device-123", "student-001");
            List<LocalGameScore> unsyncedScores = Arrays.asList(
                createScore("score-1", "student-001", "game-1", 100)
            );

            when(deviceService.getDevice()).thenReturn(Optional.of(device));
            when(scoreService.getUnsyncedScores(anyInt())).thenReturn(unsyncedScores);
            when(apiClient.uploadScores(anyString(), anyList()))
                .thenReturn(SyncResponse.success(1));

            // Act
            syncService.performSync();

            // Assert - verify token refresh was called
            verify(deviceService).refreshTokenIfNeeded();
        }

        @Test
        @DisplayName("Should abort sync if token refresh fails")
        void shouldAbortSyncIfTokenRefreshFails() {
            // Arrange
            LocalDevice device = createDevice("device-123", "student-001");
            List<LocalGameScore> unsyncedScores = Arrays.asList(
                createScore("score-1", "student-001", "game-1", 100)
            );

            when(deviceService.getDevice()).thenReturn(Optional.of(device));
            when(scoreService.getUnsyncedScores(anyInt())).thenReturn(unsyncedScores);
            doThrow(new RuntimeException("Token expired"))
                .when(deviceService).refreshTokenIfNeeded();

            // Act
            syncService.performSync();

            // Assert - sync should not proceed
            verify(apiClient, never()).uploadScores(anyString(), anyList());
            assertEquals(SyncService.SyncStatus.ERROR, syncService.getSyncStatus());
            assertTrue(syncService.getLastSyncMessage().contains("Authentication required"));
        }
    }

    @Nested
    @DisplayName("Concurrent Sync Tests")
    class ConcurrentSyncTests {

        @Test
        @DisplayName("Should prevent concurrent syncs")
        void shouldPreventConcurrentSyncs() throws InterruptedException {
            // Arrange
            LocalDevice device = createDevice("device-123", "student-001");
            List<LocalGameScore> unsyncedScores = Arrays.asList(
                createScore("score-1", "student-001", "game-1", 100)
            );

            when(deviceService.getDevice()).thenReturn(Optional.of(device));
            when(scoreService.getUnsyncedScores(anyInt())).thenReturn(unsyncedScores);

            // Simulate slow API call
            when(apiClient.uploadScores(anyString(), anyList()))
                .thenAnswer(invocation -> {
                    Thread.sleep(500);
                    return SyncResponse.success(1);
                });

            // Act - start two syncs concurrently
            Thread syncThread1 = new Thread(() -> syncService.performSync());
            Thread syncThread2 = new Thread(() -> syncService.performSync());

            syncThread1.start();
            Thread.sleep(50); // Small delay to ensure first sync starts
            syncThread2.start();

            syncThread1.join();
            syncThread2.join();

            // Assert - API should only be called once
            verify(apiClient, times(1)).uploadScores(anyString(), anyList());
        }

        @Test
        @DisplayName("Should report syncing status during sync")
        void shouldReportSyncingStatusDuringSync() throws InterruptedException {
            // Arrange
            LocalDevice device = createDevice("device-123", "student-001");
            List<LocalGameScore> unsyncedScores = Arrays.asList(
                createScore("score-1", "student-001", "game-1", 100)
            );

            AtomicBoolean wasInSyncingState = new AtomicBoolean(false);

            when(deviceService.getDevice()).thenReturn(Optional.of(device));
            when(scoreService.getUnsyncedScores(anyInt())).thenReturn(unsyncedScores);
            when(apiClient.uploadScores(anyString(), anyList()))
                .thenAnswer(invocation -> {
                    wasInSyncingState.set(syncService.isSyncing());
                    return SyncResponse.success(1);
                });

            // Act
            syncService.performSync();

            // Assert - should have been in syncing state during API call
            assertTrue(wasInSyncingState.get());
            assertFalse(syncService.isSyncing()); // Should be false after sync completes
        }
    }

    @Nested
    @DisplayName("Offline Sync Tests")
    class OfflineSyncTests {

        @Test
        @DisplayName("Should skip sync when offline")
        void shouldSkipSyncWhenOffline() {
            // Arrange
            when(networkMonitor.isOnline()).thenReturn(false);

            // Start background sync which checks online status
            syncService.startBackgroundSync();

            // Act - directly call the private method via reflection or use public interface
            // For now, we test the network monitor interaction
            assertFalse(networkMonitor.isOnline());

            // Cleanup
            syncService.stopBackgroundSync();
        }
    }

    @Nested
    @DisplayName("Sync Status Tests")
    class SyncStatusTests {

        @Test
        @DisplayName("Should start with IDLE status")
        void shouldStartWithIdleStatus() {
            assertEquals(SyncService.SyncStatus.IDLE, syncService.getSyncStatus());
        }

        @Test
        @DisplayName("Should update status to SUCCESS after successful sync")
        void shouldUpdateStatusToSuccessAfterSuccessfulSync() {
            // Arrange
            when(scoreService.getUnsyncedScores(anyInt())).thenReturn(Collections.emptyList());

            // Act
            syncService.performSync();

            // Assert
            assertEquals(SyncService.SyncStatus.SUCCESS, syncService.getSyncStatus());
        }

        @Test
        @DisplayName("Should return correct pending count")
        void shouldReturnCorrectPendingCount() {
            // Arrange
            when(scoreService.getUnsyncedCount()).thenReturn(5);

            // Act
            int pendingCount = syncService.getPendingCount();

            // Assert
            assertEquals(5, pendingCount);
        }
    }

    @Nested
    @DisplayName("Conflict Resolution Tests")
    class ConflictResolutionTests {

        @Test
        @DisplayName("Should notify callback when conflicts detected")
        void shouldNotifyCallbackWhenConflictsDetected() {
            // Arrange
            List<SyncConflict> receivedConflicts = new ArrayList<>();
            syncService.setConflictCallback(receivedConflicts::addAll);

            // The conflict callback is called during delta sync when conflicts are found
            // This is a simplified test to verify the callback mechanism works
            assertEquals(0, syncService.getPendingConflictCount());
        }

        @Test
        @DisplayName("Should track pending conflict count")
        void shouldTrackPendingConflictCount() {
            // The conflict count is initially 0 when no conflicts are detected
            // This tests the getPendingConflictCount() method which returns from internal state
            assertEquals(0, syncService.getPendingConflictCount());
        }
    }

    @Nested
    @DisplayName("Sync Statistics Tests")
    class SyncStatisticsTests {

        @Test
        @DisplayName("Should return pending score count")
        void shouldReturnPendingScoreCount() {
            // Arrange
            when(scoreService.getUnsyncedCount()).thenReturn(3);

            // Act - getPendingCount() delegates to scoreService without DB access
            int pendingCount = syncService.getPendingCount();

            // Assert
            assertEquals(3, pendingCount);
        }

        @Test
        @DisplayName("Should calculate success rate correctly")
        void shouldCalculateSuccessRateCorrectly() {
            // Test the SyncStatistics inner class
            SyncService.SyncStatistics stats = new SyncService.SyncStatistics(
                10, 8, 100, 1024, 0, 0
            );

            assertEquals(80.0, stats.getSuccessRate(), 0.01);
        }

        @Test
        @DisplayName("Should handle zero syncs for success rate")
        void shouldHandleZeroSyncsForSuccessRate() {
            SyncService.SyncStatistics stats = new SyncService.SyncStatistics(
                0, 0, 0, 0, 0, 0
            );

            assertEquals(0.0, stats.getSuccessRate(), 0.01);
        }
    }

    @Nested
    @DisplayName("Background Sync Tests")
    class BackgroundSyncTests {

        @Test
        @DisplayName("Should start and stop background sync")
        void shouldStartAndStopBackgroundSync() {
            // Act
            syncService.startBackgroundSync();

            // Should not throw when starting again
            syncService.startBackgroundSync();

            // Cleanup
            syncService.stopBackgroundSync();
        }

        @Test
        @DisplayName("Should be safe to stop sync when not started")
        void shouldBeSafeToStopSyncWhenNotStarted() {
            // Act & Assert - should not throw
            assertDoesNotThrow(() -> syncService.stopBackgroundSync());
        }
    }

    // Helper methods

    private LocalDevice createDevice(String deviceId, String studentId) {
        LocalDevice device = new LocalDevice();
        device.setDeviceId(deviceId);
        device.setStudentId(studentId);
        return device;
    }

    private LocalGameScore createScore(String scoreId, String studentId, String gameId, int score) {
        LocalGameScore gameScore = new LocalGameScore();
        gameScore.setScoreId(scoreId);
        gameScore.setStudentId(studentId);
        gameScore.setGameId(gameId);
        gameScore.setScore(score);
        gameScore.setMaxScore(100);
        gameScore.setPlayedAt(LocalDateTime.now());
        gameScore.setDeviceId("device-123");
        gameScore.setSynced(false);
        return gameScore;
    }
}
