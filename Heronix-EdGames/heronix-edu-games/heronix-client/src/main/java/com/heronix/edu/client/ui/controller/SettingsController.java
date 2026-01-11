package com.heronix.edu.client.ui.controller;

import com.heronix.edu.client.config.AppConfig;
import com.heronix.edu.client.db.entity.LocalDevice;
import com.heronix.edu.client.service.DeviceService;
import com.heronix.edu.client.service.SyncService;
import com.heronix.edu.client.util.NetworkMonitor;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the settings screen
 */
public class SettingsController {
    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

    @FXML private Label deviceIdLabel;
    @FXML private Label deviceNameLabel;
    @FXML private Label studentIdLabel;
    @FXML private Label deviceStatusLabel;
    @FXML private Label serverUrlLabel;
    @FXML private Label syncIntervalLabel;
    @FXML private Label networkStatusLabel;
    @FXML private Label appVersionLabel;
    @FXML private Label dbPathLabel;
    @FXML private Button closeButton;
    @FXML private Button testConnectionButton;
    @FXML private Button forceSyncButton;
    @FXML private Label statusLabel;

    private DeviceService deviceService;
    private SyncService syncService;
    private NetworkMonitor networkMonitor;
    private Stage stage;

    /**
     * Initialize controller with dependencies
     */
    public void initialize(DeviceService deviceService, SyncService syncService, Stage stage) {
        this.deviceService = deviceService;
        this.syncService = syncService;
        this.stage = stage;
        this.networkMonitor = NetworkMonitor.getInstance();

        loadSettings();

        logger.info("Settings controller initialized");
    }

    /**
     * Load and display settings
     */
    private void loadSettings() {
        try {
            // Device information
            LocalDevice device = deviceService.getDevice()
                .orElseThrow(() -> new IllegalStateException("Device not found"));

            deviceIdLabel.setText(device.getDeviceId());
            deviceNameLabel.setText(device.getDeviceName());
            studentIdLabel.setText(device.getStudentId() != null ? device.getStudentId() : "Not assigned");
            deviceStatusLabel.setText(device.getStatus());

            // Style status
            if ("APPROVED".equals(device.getStatus())) {
                deviceStatusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            } else if ("PENDING".equals(device.getStatus())) {
                deviceStatusLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
            } else {
                deviceStatusLabel.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
            }

            // Application settings
            serverUrlLabel.setText(AppConfig.getServerUrl());
            syncIntervalLabel.setText(AppConfig.getSyncIntervalMinutes() + " minutes");
            appVersionLabel.setText(AppConfig.getAppVersion());
            dbPathLabel.setText(AppConfig.getDatabasePath());

            // Network status
            updateNetworkStatus();

        } catch (Exception e) {
            logger.error("Error loading settings", e);
            showError("Failed to load settings: " + e.getMessage());
        }
    }

    /**
     * Update network status display
     */
    private void updateNetworkStatus() {
        boolean online = networkMonitor.isOnline();
        if (online) {
            networkStatusLabel.setText("Online");
            networkStatusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        } else {
            networkStatusLabel.setText("Offline");
            networkStatusLabel.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
        }
    }

    /**
     * Handle test connection button
     */
    @FXML
    public void handleTestConnection() {
        logger.info("Testing server connection");
        statusLabel.setText("Testing connection...");
        statusLabel.setStyle("-fx-text-fill: #2196F3;");

        new Thread(() -> {
            try {
                // Simple ping test
                boolean success = deviceService.testServerConnection();

                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        showInfo("Connection successful!");
                    } else {
                        showError("Connection failed - server not reachable");
                    }
                    updateNetworkStatus();
                });

            } catch (Exception e) {
                logger.error("Connection test failed", e);
                javafx.application.Platform.runLater(() -> {
                    showError("Connection test failed: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Handle force sync button
     */
    @FXML
    public void handleForceSync() {
        logger.info("Manual sync requested from settings");

        if (!networkMonitor.isOnline()) {
            showError("Cannot sync: No network connection");
            return;
        }

        if (syncService.isSyncing()) {
            showInfo("Sync already in progress");
            return;
        }

        statusLabel.setText("Syncing...");
        statusLabel.setStyle("-fx-text-fill: #2196F3;");

        new Thread(() -> {
            syncService.performSync();
            javafx.application.Platform.runLater(() -> {
                showInfo(syncService.getLastSyncMessage());
            });
        }).start();
    }

    /**
     * Handle close button
     */
    @FXML
    public void handleClose() {
        logger.info("Closing settings");
        stage.close();
    }

    /**
     * Show info message
     */
    private void showInfo(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #2196F3;");
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #f44336;");
    }
}
