package com.heronix.edu.client.ui.controller;

import com.heronix.edu.client.multiplayer.StompGameClient;
import com.heronix.edu.client.multiplayer.dto.AvatarInfo;
import com.heronix.edu.client.service.DeviceService;
import com.heronix.edu.client.service.ScoreService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Controller for the Code Breaker join session screen.
 * Handles session code entry, avatar selection, and secret code choice.
 */
public class CodeBreakerJoinController {
    private static final Logger logger = LoggerFactory.getLogger(CodeBreakerJoinController.class);

    @FXML private TextField sessionCodeField;
    @FXML private TextField studentNameField;
    @FXML private FlowPane avatarGrid;
    @FXML private Label avatarBonusLabel;
    @FXML private HBox secretCodeBox;
    @FXML private Button joinButton;
    @FXML private Label statusLabel;

    private DeviceService deviceService;
    private ScoreService scoreService;
    private Stage stage;
    private Runnable onBack;

    private String selectedAvatarId = null;
    private String selectedSecretCode = null;
    private ToggleGroup avatarToggleGroup;
    private ToggleGroup secretCodeToggleGroup;

    // Available secret codes
    private static final String[] SECRET_CODES = {"ALPHA", "BETA", "GAMMA", "DELTA", "OMEGA"};

    /**
     * Initialize controller with dependencies.
     */
    public void initialize(DeviceService deviceService, ScoreService scoreService, Stage stage, Runnable onBack) {
        this.deviceService = deviceService;
        this.scoreService = scoreService;
        this.stage = stage;
        this.onBack = onBack;

        setupAvatarGrid();
        setupSecretCodeSelection();
        setupValidation();

        // Pre-fill student name if available
        deviceService.getDevice().ifPresent(device -> {
            if (device.getStudentId() != null) {
                studentNameField.setText("Student " + device.getStudentId().substring(0, Math.min(4, device.getStudentId().length())));
            }
        });

        logger.info("CodeBreakerJoinController initialized");
    }

    /**
     * Setup avatar selection grid.
     */
    private void setupAvatarGrid() {
        avatarToggleGroup = new ToggleGroup();
        AvatarInfo[] avatars = AvatarInfo.getAvailableAvatars();

        for (AvatarInfo avatar : avatars) {
            VBox avatarCard = createAvatarCard(avatar);
            avatarGrid.getChildren().add(avatarCard);
        }
    }

    /**
     * Create an avatar selection card.
     */
    private VBox createAvatarCard(AvatarInfo avatar) {
        VBox card = new VBox(5);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-padding: 10; -fx-background-radius: 10; -fx-cursor: hand;");
        card.setPrefWidth(70);
        card.setPrefHeight(80);

        // Emoji
        Label emojiLabel = new Label(avatar.getEmoji());
        emojiLabel.setStyle("-fx-font-size: 28px;");

        // Name
        Label nameLabel = new Label(avatar.getName().split(" ")[0]); // First word only
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 10px;");

        card.getChildren().addAll(emojiLabel, nameLabel);

        // Toggle behavior
        ToggleButton toggle = new ToggleButton();
        toggle.setToggleGroup(avatarToggleGroup);
        toggle.setVisible(false);
        toggle.setUserData(avatar);

        card.setOnMouseClicked(e -> {
            toggle.setSelected(true);
            selectedAvatarId = avatar.getAvatarId();
            avatarBonusLabel.setText(avatar.getEmoji() + " " + avatar.getBonusDescription());
            avatarBonusLabel.setStyle("-fx-text-fill: #00ff88; -fx-font-size: 12px;");

            // Update visual selection
            avatarGrid.getChildren().forEach(node -> {
                if (node instanceof VBox) {
                    node.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-padding: 10; -fx-background-radius: 10; -fx-cursor: hand;");
                }
            });
            card.setStyle("-fx-background-color: rgba(0,255,136,0.3); -fx-padding: 10; -fx-background-radius: 10; -fx-cursor: hand; -fx-border-color: #00ff88; -fx-border-width: 2; -fx-border-radius: 10;");

            validateForm();
        });

        return card;
    }

    /**
     * Setup secret code selection buttons.
     */
    private void setupSecretCodeSelection() {
        secretCodeToggleGroup = new ToggleGroup();

        for (String code : SECRET_CODES) {
            ToggleButton codeBtn = new ToggleButton(code);
            codeBtn.setToggleGroup(secretCodeToggleGroup);
            codeBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 10 15; -fx-background-radius: 5; -fx-cursor: hand;");
            codeBtn.setPrefWidth(80);

            codeBtn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    selectedSecretCode = code;
                    codeBtn.setStyle("-fx-background-color: #00ff88; -fx-text-fill: #1a237e; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 10 15; -fx-background-radius: 5;");
                } else {
                    codeBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 10 15; -fx-background-radius: 5; -fx-cursor: hand;");
                }
                validateForm();
            });

            secretCodeBox.getChildren().add(codeBtn);
        }
    }

    /**
     * Setup form validation listeners.
     */
    private void setupValidation() {
        sessionCodeField.textProperty().addListener((obs, old, newVal) -> {
            // Auto-uppercase and limit to 6 chars
            if (newVal.length() > 6) {
                sessionCodeField.setText(newVal.substring(0, 6).toUpperCase());
            } else {
                sessionCodeField.setText(newVal.toUpperCase());
            }
            validateForm();
        });

        studentNameField.textProperty().addListener((obs, old, newVal) -> validateForm());
    }

    /**
     * Validate form and enable/disable join button.
     */
    private void validateForm() {
        boolean valid = sessionCodeField.getText().length() == 6
                && !studentNameField.getText().trim().isEmpty()
                && selectedAvatarId != null
                && selectedSecretCode != null;

        joinButton.setDisable(!valid);
    }

    /**
     * Handle join game button click.
     */
    @FXML
    public void handleJoinGame() {
        String sessionCode = sessionCodeField.getText().toUpperCase();
        String studentName = studentNameField.getText().trim();
        String studentId = deviceService.getDevice()
                .map(d -> d.getStudentId())
                .orElse("STUDENT_" + System.currentTimeMillis());

        statusLabel.setText("Connecting to game server...");
        statusLabel.setStyle("-fx-text-fill: #00ff88;");
        joinButton.setDisable(true);

        // Connect to WebSocket in background
        new Thread(() -> {
            try {
                // Get server URL from config (default to localhost)
                String serverUrl = "http://localhost:8081";

                StompGameClient wsClient = new StompGameClient(serverUrl);

                // Set up connection handler
                wsClient.setConnectionStatusHandler(connected -> {
                    Platform.runLater(() -> {
                        if (connected) {
                            statusLabel.setText("Connected! Joining session...");
                            // Join the session
                            wsClient.joinSession(sessionCode, studentId, studentName, selectedSecretCode, selectedAvatarId);
                        } else {
                            statusLabel.setText("Disconnected from server");
                            statusLabel.setStyle("-fx-text-fill: #ff5252;");
                            joinButton.setDisable(false);
                        }
                    });
                });

                // Set up message handlers
                wsClient.onMessage("JOIN_RESPONSE", msg -> {
                    Platform.runLater(() -> {
                        Boolean success = (Boolean) msg.get("success");
                        if (success != null && success) {
                            statusLabel.setText("Joined! Waiting for game to start...");
                            String playerId = (String) msg.get("playerId");
                            wsClient.setPlayerId(playerId);
                            launchGameScreen(wsClient, sessionCode, studentName);
                        } else {
                            String error = (String) msg.get("error");
                            statusLabel.setText("Failed to join: " + (error != null ? error : "Unknown error"));
                            statusLabel.setStyle("-fx-text-fill: #ff5252;");
                            joinButton.setDisable(false);
                        }
                    });
                });

                // Connect and wait for result
                wsClient.connect().thenAccept(connected -> {
                    if (!connected) {
                        Platform.runLater(() -> {
                            statusLabel.setText("Connection failed");
                            statusLabel.setStyle("-fx-text-fill: #ff5252;");
                            joinButton.setDisable(false);
                        });
                    }
                });

            } catch (Exception e) {
                logger.error("Failed to connect to game server", e);
                Platform.runLater(() -> {
                    statusLabel.setText("Connection failed: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #ff5252;");
                    joinButton.setDisable(false);
                });
            }
        }).start();
    }

    /**
     * Launch the main game screen.
     */
    private void launchGameScreen(StompGameClient wsClient, String sessionCode, String studentName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/code-breaker-game.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);

            CodeBreakerGameController gameController = loader.getController();
            gameController.initialize(wsClient, sessionCode, studentName, selectedAvatarId, selectedSecretCode,
                                      scoreService, stage, onBack);

            stage.setScene(scene);
            stage.setTitle("Code Breaker - " + sessionCode);

        } catch (Exception e) {
            logger.error("Failed to load game screen", e);
            statusLabel.setText("Failed to load game: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #ff5252;");
            joinButton.setDisable(false);
        }
    }

    /**
     * Handle back button.
     */
    @FXML
    public void handleBack() {
        if (onBack != null) {
            onBack.run();
        }
    }
}
