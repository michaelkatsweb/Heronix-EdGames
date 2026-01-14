package com.heronix.edu.client.ui.controller;

import com.heronix.edu.client.db.entity.LocalDevice;
import com.heronix.edu.client.service.DeviceService;
import com.heronix.edu.client.service.GameManager;
import com.heronix.edu.client.service.ScoreService;
import com.heronix.edu.client.service.SyncService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Controller for the waiting for approval screen
 * Polls server for device approval status
 */
public class WaitingForApprovalController {
    private static final Logger logger = LoggerFactory.getLogger(WaitingForApprovalController.class);
    private static final int POLL_INTERVAL_SECONDS = 5;

    @FXML private Label statusLabel;
    @FXML private Label deviceNameLabel;
    @FXML private Label deviceIdLabel;
    @FXML private Label studentIdLabel;
    @FXML private Button checkNowButton;
    @FXML private ProgressIndicator progressIndicator;

    private DeviceService deviceService;
    private GameManager gameManager;
    private ScoreService scoreService;
    private SyncService syncService;
    private Stage stage;
    private ScheduledExecutorService scheduler;

    /**
     * Initialize controller with dependencies
     * Called from SetupWizardController or HeronixClientApplication
     */
    public void initialize(DeviceService deviceService, GameManager gameManager,
                          ScoreService scoreService, SyncService syncService, Stage stage) {
        this.deviceService = deviceService;
        this.gameManager = gameManager;
        this.scoreService = scoreService;
        this.syncService = syncService;
        this.stage = stage;

        // Load device info
        LocalDevice device = deviceService.getDevice()
            .orElseThrow(() -> new IllegalStateException("Device not found"));

        deviceNameLabel.setText(device.getDeviceName());
        deviceIdLabel.setText(device.getDeviceId().substring(0, 8) + "...");
        studentIdLabel.setText(device.getStudentId() != null ? device.getStudentId() : "Pending");

        // Start polling for approval
        startPolling();

        logger.info("Waiting for approval screen initialized");
    }

    /**
     * Start background polling for approval status
     */
    private void startPolling() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(
            this::checkApprovalStatus,
            0,
            POLL_INTERVAL_SECONDS,
            TimeUnit.SECONDS
        );

        logger.info("Started polling for approval every {} seconds", POLL_INTERVAL_SECONDS);
    }

    /**
     * Check approval status with server
     */
    private void checkApprovalStatus() {
        try {
            logger.debug("Checking device approval status");

            String status = deviceService.checkApprovalStatus();

            Platform.runLater(() -> {
                updateStatusLabel(status);

                if ("APPROVED".equals(status)) {
                    handleApproved();
                } else if ("REJECTED".equals(status)) {
                    handleRejected();
                } else if ("REVOKED".equals(status)) {
                    handleRevoked();
                }
            });

        } catch (com.heronix.edu.client.api.exception.ApiException e) {
            logger.error("Error checking approval status", e);

            // Handle 404 - device not found (likely deleted by admin)
            if (e.getMessage().contains("404") || e.getMessage().contains("not found")) {
                logger.warn("Device not found on server - registration may have been deleted");
                Platform.runLater(() -> {
                    statusLabel.setText("Device registration not found. Please contact your teacher or re-register.");
                    statusLabel.setStyle("-fx-text-fill: #f44336;");
                    stopPolling(); // Stop polling for a non-existent device
                });
            } else {
                Platform.runLater(() -> {
                    statusLabel.setText("Error checking status: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #f44336;");
                });
            }
        } catch (Exception e) {
            logger.error("Error checking approval status", e);
            Platform.runLater(() -> {
                statusLabel.setText("Error checking status. Will retry...");
                statusLabel.setStyle("-fx-text-fill: #FF9800;");
            });
        }
    }

    /**
     * Handle manual check button
     */
    @FXML
    public void handleCheckNow() {
        checkNowButton.setDisable(true);
        statusLabel.setText("Checking status...");
        statusLabel.setStyle("-fx-text-fill: #1976D2;");

        new Thread(() -> {
            checkApprovalStatus();
            Platform.runLater(() -> checkNowButton.setDisable(false));
        }).start();
    }

    /**
     * Handle device approval
     */
    private void handleApproved() {
        logger.info("Device approved!");
        statusLabel.setText("Device approved! Starting games...");
        statusLabel.setStyle("-fx-text-fill: #4CAF50;");

        stopPolling();

        // Authenticate and transition to main launcher
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                deviceService.authenticateDevice();

                Platform.runLater(() -> {
                    try {
                        showMainLauncher();
                    } catch (Exception e) {
                        logger.error("Error loading main launcher", e);
                        statusLabel.setText("Error: " + e.getMessage());
                        statusLabel.setStyle("-fx-text-fill: #f44336;");
                    }
                });

            } catch (Exception e) {
                logger.error("Error authenticating device", e);
                Platform.runLater(() -> {
                    statusLabel.setText("Authentication failed: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #f44336;");
                });
            }
        }).start();
    }

    /**
     * Handle device rejection
     */
    private void handleRejected() {
        logger.warn("Device was rejected");
        statusLabel.setText("Device was rejected by teacher. Please contact your teacher.");
        statusLabel.setStyle("-fx-text-fill: #f44336;");
        stopPolling();
    }

    /**
     * Handle device revocation
     */
    private void handleRevoked() {
        logger.warn("Device was revoked");
        statusLabel.setText("Device access was revoked. Please contact your teacher.");
        statusLabel.setStyle("-fx-text-fill: #f44336;");
        stopPolling();
    }

    /**
     * Update status label based on device status
     */
    private void updateStatusLabel(String status) {
        switch (status) {
            case "PENDING":
                statusLabel.setText("Still waiting for teacher approval...");
                statusLabel.setStyle("-fx-text-fill: #1976D2;");
                break;
            case "APPROVED":
                statusLabel.setText("Device approved!");
                statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                break;
            case "REJECTED":
                statusLabel.setText("Device was rejected");
                statusLabel.setStyle("-fx-text-fill: #f44336;");
                break;
            case "REVOKED":
                statusLabel.setText("Device access was revoked");
                statusLabel.setStyle("-fx-text-fill: #f44336;");
                break;
            default:
                statusLabel.setText("Unknown status: " + status);
                statusLabel.setStyle("-fx-text-fill: #666;");
        }
    }

    /**
     * Show main launcher
     */
    private void showMainLauncher() throws Exception {
        logger.info("Loading main launcher");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main-launcher.fxml"));
        Scene scene = new Scene(loader.load(), 1024, 768);

        // Pass dependencies to controller
        MainLauncherController controller = loader.getController();
        controller.initialize(deviceService, gameManager, scoreService, syncService, stage);

        stage.setScene(scene);
        stage.setTitle("Heronix Educational Games");
    }

    /**
     * Stop polling when controller is destroyed
     */
    private void stopPolling() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            logger.info("Stopped polling for approval");
        }
    }
}
