package com.heronix.edu.client.ui.component;

import com.heronix.edu.client.service.SyncService;
import com.heronix.edu.client.service.SyncService.SyncStatus;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A visual indicator component showing sync status.
 * Displays:
 * - Current sync state (idle, syncing, success, error, conflict)
 * - Number of pending items
 * - Clickable to trigger manual sync or show conflicts
 */
public class SyncStatusIndicator extends HBox {

    private final Circle statusDot;
    private final Label statusLabel;
    private final Label pendingLabel;
    private final ProgressIndicator syncProgress;

    private SyncService syncService;
    private ScheduledExecutorService updateScheduler;
    private Runnable onConflictClick;
    private Runnable onSyncClick;

    // Colors for different states
    private static final String COLOR_IDLE = "#9E9E9E";      // Gray
    private static final String COLOR_SYNCING = "#2196F3";   // Blue
    private static final String COLOR_SUCCESS = "#4CAF50";   // Green
    private static final String COLOR_ERROR = "#F44336";     // Red
    private static final String COLOR_CONFLICT = "#FF9800";  // Orange

    public SyncStatusIndicator() {
        super(8); // spacing
        setAlignment(Pos.CENTER_LEFT);

        // Status dot
        statusDot = new Circle(6);
        statusDot.setFill(Color.web(COLOR_IDLE));

        // Sync progress (shown during sync)
        syncProgress = new ProgressIndicator(-1);
        syncProgress.setPrefSize(16, 16);
        syncProgress.setVisible(false);

        // Status label
        statusLabel = new Label("Idle");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        // Pending count label
        pendingLabel = new Label("");
        pendingLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");

        getChildren().addAll(statusDot, syncProgress, statusLabel, pendingLabel);

        // Tooltip
        Tooltip tooltip = new Tooltip("Click to sync or view conflicts");
        Tooltip.install(this, tooltip);

        // Make clickable
        setStyle("-fx-cursor: hand;");
        setOnMouseClicked(e -> handleClick());

        // Hover effect
        setOnMouseEntered(e -> setStyle("-fx-cursor: hand; -fx-background-color: rgba(0,0,0,0.05); -fx-background-radius: 4;"));
        setOnMouseExited(e -> setStyle("-fx-cursor: hand;"));
    }

    /**
     * Initialize with sync service and start auto-updates
     */
    public void initialize(SyncService syncService) {
        this.syncService = syncService;
        startAutoUpdate();
    }

    /**
     * Set callback for when user clicks while conflicts are pending
     */
    public void setOnConflictClick(Runnable callback) {
        this.onConflictClick = callback;
    }

    /**
     * Set callback for when user clicks to trigger sync
     */
    public void setOnSyncClick(Runnable callback) {
        this.onSyncClick = callback;
    }

    /**
     * Start auto-updating the status display
     */
    public void startAutoUpdate() {
        if (updateScheduler != null && !updateScheduler.isShutdown()) {
            return;
        }

        updateScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SyncStatusUpdater");
            t.setDaemon(true);
            return t;
        });

        updateScheduler.scheduleAtFixedRate(
            () -> Platform.runLater(this::updateDisplay),
            0, 2, TimeUnit.SECONDS
        );
    }

    /**
     * Stop auto-updates
     */
    public void stopAutoUpdate() {
        if (updateScheduler != null && !updateScheduler.isShutdown()) {
            updateScheduler.shutdown();
        }
    }

    /**
     * Update the display based on current sync status
     */
    public void updateDisplay() {
        if (syncService == null) return;

        SyncStatus status = syncService.getSyncStatus();
        int pendingCount = syncService.getPendingCount();
        int conflictCount = syncService.getPendingConflictCount();
        String message = syncService.getLastSyncMessage();

        // Update UI based on status
        switch (status) {
            case IDLE:
                setIdleState(pendingCount);
                break;
            case SYNCING:
                setSyncingState();
                break;
            case SUCCESS:
                setSuccessState(pendingCount);
                break;
            case ERROR:
                setErrorState(message);
                break;
            case CONFLICT:
                setConflictState(conflictCount);
                break;
        }

        // Update pending count
        if (pendingCount > 0 && status != SyncStatus.SYNCING) {
            pendingLabel.setText("(" + pendingCount + " pending)");
        } else if (conflictCount > 0) {
            pendingLabel.setText("(" + conflictCount + " conflict" + (conflictCount != 1 ? "s" : "") + ")");
        } else {
            pendingLabel.setText("");
        }
    }

    private void setIdleState(int pendingCount) {
        statusDot.setVisible(true);
        syncProgress.setVisible(false);

        if (pendingCount > 0) {
            statusDot.setFill(Color.web(COLOR_IDLE));
            statusLabel.setText("Pending sync");
        } else {
            statusDot.setFill(Color.web(COLOR_SUCCESS));
            statusLabel.setText("Synced");
        }
    }

    private void setSyncingState() {
        statusDot.setVisible(false);
        syncProgress.setVisible(true);
        statusLabel.setText("Syncing...");
        pendingLabel.setText("");
    }

    private void setSuccessState(int pendingCount) {
        statusDot.setVisible(true);
        syncProgress.setVisible(false);
        statusDot.setFill(Color.web(COLOR_SUCCESS));

        if (pendingCount > 0) {
            statusLabel.setText("Partially synced");
        } else {
            statusLabel.setText("Synced");
            // Flash green animation
            animateStatusDot(Color.web(COLOR_SUCCESS));
        }
    }

    private void setErrorState(String message) {
        statusDot.setVisible(true);
        syncProgress.setVisible(false);
        statusDot.setFill(Color.web(COLOR_ERROR));
        statusLabel.setText("Sync failed");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #F44336;");

        // Update tooltip with error
        Tooltip.install(this, new Tooltip("Error: " + message + "\nClick to retry"));
    }

    private void setConflictState(int conflictCount) {
        statusDot.setVisible(true);
        syncProgress.setVisible(false);
        statusDot.setFill(Color.web(COLOR_CONFLICT));
        statusLabel.setText("Conflicts");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #FF9800; -fx-font-weight: bold;");

        // Pulse animation for attention
        animatePulse();

        Tooltip.install(this, new Tooltip(conflictCount + " conflict(s) need resolution. Click to resolve."));
    }

    private void animateStatusDot(Color targetColor) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(statusDot.fillProperty(), Color.WHITE)),
            new KeyFrame(Duration.millis(200),
                new KeyValue(statusDot.fillProperty(), targetColor)),
            new KeyFrame(Duration.millis(400),
                new KeyValue(statusDot.fillProperty(), Color.WHITE)),
            new KeyFrame(Duration.millis(600),
                new KeyValue(statusDot.fillProperty(), targetColor))
        );
        timeline.play();
    }

    private void animatePulse() {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(statusDot.scaleXProperty(), 1.0),
                new KeyValue(statusDot.scaleYProperty(), 1.0)),
            new KeyFrame(Duration.millis(500),
                new KeyValue(statusDot.scaleXProperty(), 1.3),
                new KeyValue(statusDot.scaleYProperty(), 1.3)),
            new KeyFrame(Duration.millis(1000),
                new KeyValue(statusDot.scaleXProperty(), 1.0),
                new KeyValue(statusDot.scaleYProperty(), 1.0))
        );
        timeline.setCycleCount(3);
        timeline.play();
    }

    private void handleClick() {
        if (syncService == null) return;

        SyncStatus status = syncService.getSyncStatus();

        if (status == SyncStatus.CONFLICT && onConflictClick != null) {
            // Show conflicts dialog
            onConflictClick.run();
        } else if (status != SyncStatus.SYNCING && onSyncClick != null) {
            // Trigger sync
            onSyncClick.run();
        }
    }

    /**
     * Force refresh the display
     */
    public void refresh() {
        Platform.runLater(this::updateDisplay);
    }

    /**
     * Clean up resources
     */
    public void dispose() {
        stopAutoUpdate();
    }
}
