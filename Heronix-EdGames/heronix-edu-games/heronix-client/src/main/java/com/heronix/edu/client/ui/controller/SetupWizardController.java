package com.heronix.edu.client.ui.controller;

import com.heronix.edu.client.service.DeviceService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the setup wizard screen
 * Handles device registration using hardware-based device fingerprinting
 *
 * NEW WORKFLOW:
 * 1. Student enters their name only (no password, no registration code)
 * 2. System automatically captures hardware ID (CPU/MAC/Motherboard)
 * 3. Device registers as PENDING
 * 4. Teacher approves and assigns official student ID
 * 5. Hardware ID becomes the authentication credential
 */
public class SetupWizardController {
    private static final Logger logger = LoggerFactory.getLogger(SetupWizardController.class);

    @FXML private TextField deviceNameField;
    @FXML private Button registerButton;
    @FXML private Label statusLabel;
    @FXML private Label hardwareIdLabel;  // New: Display hardware ID
    @FXML private ProgressIndicator progressIndicator;

    private DeviceService deviceService;
    private Stage stage;

    /**
     * Initialize controller with dependencies
     * Called from HeronixClientApplication
     */
    public void initialize(DeviceService deviceService, Stage stage) {
        this.deviceService = deviceService;
        this.stage = stage;

        // Set default device name suggestion
        String defaultName = System.getProperty("user.name");
        deviceNameField.setPromptText("Enter your name (e.g., " + defaultName + ")");

        // Display hardware ID (first 12 chars for verification)
        String hwId = deviceService.getOrCreateDeviceId();
        if (hardwareIdLabel != null && hwId != null && hwId.length() >= 12) {
            hardwareIdLabel.setText("Device ID: " + hwId.substring(0, 12) + "...");
        }

        logger.info("Setup wizard initialized (hardware-based registration)");
    }

    /**
     * Handle register button click
     * NEW: No registration code required - hardware ID is the credential
     */
    @FXML
    public void handleRegister() {
        String deviceName = deviceNameField.getText().trim();

        // Validate input - only student name required
        if (deviceName.isEmpty()) {
            showError("Please enter your name (first name, last name, or nickname)");
            return;
        }

        // Disable form during registration
        setFormEnabled(false);
        progressIndicator.setVisible(true);
        statusLabel.setText("Registering device with hardware ID...");
        statusLabel.setStyle("-fx-text-fill: #1976D2;");

        // Register in background thread
        // No registration code needed - hardware ID is captured automatically
        new Thread(() -> {
            try {
                deviceService.registerDevice(null, deviceName);  // registrationCode = null

                Platform.runLater(() -> {
                    logger.info("Device registered successfully with hardware fingerprint");
                    statusLabel.setText("Device registered! Waiting for teacher approval...");
                    statusLabel.setStyle("-fx-text-fill: #4CAF50;");

                    // Transition to waiting screen after brief delay
                    try {
                        Thread.sleep(1500);
                        showWaitingScreen();
                    } catch (Exception e) {
                        logger.error("Error transitioning to waiting screen", e);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    logger.error("Registration failed", e);
                    String errorMsg = e.getMessage();
                    if (errorMsg != null && errorMsg.contains("already registered")) {
                        showError("This device is already registered. Contact your teacher if you need help.");
                    } else {
                        showError("Registration failed: " + errorMsg);
                    }
                    setFormEnabled(true);
                    progressIndicator.setVisible(false);
                });
            }
        }).start();
    }

    /**
     * Show waiting for approval screen
     */
    private void showWaitingScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/waiting-for-approval.fxml"));
            Scene scene = new Scene(loader.load(), 600, 300);

            WaitingForApprovalController controller = loader.getController();
            controller.initialize(deviceService, stage);

            stage.setScene(scene);

        } catch (Exception e) {
            logger.error("Failed to load waiting screen", e);
            showError("Failed to load next screen: " + e.getMessage());
        }
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #f44336;");
    }

    /**
     * Enable or disable form controls
     */
    private void setFormEnabled(boolean enabled) {
        deviceNameField.setDisable(!enabled);
        registerButton.setDisable(!enabled);
    }
}
