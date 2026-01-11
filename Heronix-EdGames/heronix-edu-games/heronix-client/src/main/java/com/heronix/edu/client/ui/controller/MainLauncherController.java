package com.heronix.edu.client.ui.controller;

import com.heronix.edu.client.api.dto.GameInfoDto;
import com.heronix.edu.client.db.entity.InstalledGame;
import com.heronix.edu.client.db.entity.LocalDevice;
import com.heronix.edu.client.db.repository.GameScoreRepository;
import com.heronix.edu.client.db.repository.InstalledGameRepository;
import com.heronix.edu.client.game.GameLauncher;
import com.heronix.edu.client.service.DeviceService;
import com.heronix.edu.client.service.GameManager;
import com.heronix.edu.client.service.ScoreService;
import com.heronix.edu.client.service.SyncService;
import com.heronix.edu.client.ui.component.GameCard;
import com.heronix.edu.client.util.NetworkMonitor;
import com.heronix.edu.common.game.DifficultyLevel;
import com.heronix.edu.common.game.EducationalGame;
import com.heronix.edu.common.game.GameContext;
import com.heronix.edu.common.model.Student;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controller for the main launcher screen
 * Displays installed games and allows launching them
 */
public class MainLauncherController {
    private static final Logger logger = LoggerFactory.getLogger(MainLauncherController.class);

    @FXML private Label studentNameLabel;
    @FXML private Label gradeLabel;
    @FXML private Label syncStatusIcon;
    @FXML private Label syncStatusLabel;
    @FXML private Label statusLabel;
    @FXML private Label pendingScoresLabel;
    @FXML private Button settingsButton;
    @FXML private Button refreshButton;
    @FXML private Button refreshStoreButton;
    @FXML private Button syncNowButton;
    @FXML private FlowPane gamesGrid;
    @FXML private FlowPane storeGamesGrid;
    @FXML private TableView scoresTable;

    private DeviceService deviceService;
    private GameManager gameManager;
    private ScoreService scoreService;
    private SyncService syncService;
    private NetworkMonitor networkMonitor;
    private Stage stage;

    /**
     * Initialize controller with dependencies
     */
    public void initialize(DeviceService deviceService, GameManager gameManager,
                          ScoreService scoreService, SyncService syncService, Stage stage) {
        this.deviceService = deviceService;
        this.gameManager = gameManager;
        this.scoreService = scoreService;
        this.syncService = syncService;
        this.stage = stage;
        this.networkMonitor = NetworkMonitor.getInstance();

        // Start network monitoring
        networkMonitor.startMonitoring();

        // Load student info
        loadStudentInfo();

        // Load games
        loadGames();

        // Load available games from store
        loadStoreGames();

        // Update sync status
        updateSyncStatus();

        // Update status periodically
        startStatusUpdates();

        logger.info("Main launcher initialized");
    }

    /**
     * Load student information
     */
    private void loadStudentInfo() {
        try {
            LocalDevice device = deviceService.getDevice()
                .orElseThrow(() -> new IllegalStateException("Device not found"));

            // For now, just show device name (student info would come from server)
            studentNameLabel.setText("Device: " + device.getDeviceName());
            gradeLabel.setText("Student ID: " + device.getStudentId());

        } catch (Exception e) {
            logger.error("Error loading student info", e);
            studentNameLabel.setText("Unknown Student");
            gradeLabel.setText("");
        }
    }

    /**
     * Load and display installed games
     */
    private void loadGames() {
        logger.info("Loading installed games");

        gamesGrid.getChildren().clear();

        try {
            List<InstalledGame> games = gameManager.getInstalledGames();

            if (games.isEmpty()) {
                Label emptyLabel = new Label("No games installed yet.\nGames will be available here once installed.");
                emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #999; -fx-padding: 40;");
                gamesGrid.getChildren().add(emptyLabel);
                statusLabel.setText("No games installed");
                return;
            }

            // Create game cards
            for (InstalledGame game : games) {
                GameCard card = new GameCard(game);
                card.setOnPlay(() -> launchGame(game));
                gamesGrid.getChildren().add(card);
            }

            statusLabel.setText(games.size() + " game(s) installed");
            logger.info("Loaded {} games", games.size());

        } catch (Exception e) {
            logger.error("Error loading games", e);
            showError("Failed to load games: " + e.getMessage());
        }
    }

    /**
     * Launch a game
     */
    private void launchGame(InstalledGame installedGame) {
        logger.info("Launching game: {}", installedGame.getGameName());

        try {
            // Load game instance
            EducationalGame game = gameManager.loadGame(installedGame.getGameId());

            // Create game context
            LocalDevice device = deviceService.getDevice()
                .orElseThrow(() -> new IllegalStateException("Device not found"));

            // Create a minimal Student object for game context
            // In a full implementation, this would come from server
            Student student = new Student();
            student.setStudentId(device.getStudentId());
            student.setFirstName("Student"); // Placeholder
            student.setLastInitial("S"); // Placeholder
            student.setGradeLevel("5"); // Placeholder

            GameContext context = new GameContext(student, device.getDeviceId());
            context.setDifficultyLevel(DifficultyLevel.MEDIUM);
            context.setTimeLimit(0); // No time limit

            // Create launcher
            GameLauncher launcher = new GameLauncher(game, context);

            // Launch game with completion handler
            launcher.launch(result -> {
                logger.info("Game completed: {}", installedGame.getGameName());

                // Save score
                scoreService.saveGameScore(result);

                // Update last played
                gameManager.updateLastPlayed(installedGame.getGameId());

                // Refresh UI
                Platform.runLater(() -> {
                    loadGames();
                    updateSyncStatus();
                    showInfo("Score saved! " + result.getScore() + "/" + result.getMaxScore());
                });
            });

        } catch (Exception e) {
            logger.error("Error launching game", e);
            showError("Failed to launch game: " + e.getMessage());
        }
    }

    /**
     * Update sync status display
     */
    private void updateSyncStatus() {
        int pendingCount = scoreService.getUnsyncedCount();
        boolean online = networkMonitor.isOnline();

        // Update icon
        if (online) {
            syncStatusIcon.setText("●");
            syncStatusIcon.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 16px;");
            syncStatusLabel.setText("Online");
        } else {
            syncStatusIcon.setText("●");
            syncStatusIcon.setStyle("-fx-text-fill: #f44336; -fx-font-size: 16px;");
            syncStatusLabel.setText("Offline");
        }

        // Update pending scores
        if (pendingCount > 0) {
            pendingScoresLabel.setText(pendingCount + " score(s) pending sync");
            pendingScoresLabel.setStyle("-fx-text-fill: #FF9800;");
        } else {
            pendingScoresLabel.setText("All scores synced");
            pendingScoresLabel.setStyle("-fx-text-fill: #4CAF50;");
        }
    }

    /**
     * Start periodic status updates
     */
    private void startStatusUpdates() {
        Thread updateThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000); // Update every 5 seconds
                    Platform.runLater(this::updateSyncStatus);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        updateThread.setDaemon(true);
        updateThread.start();
    }

    /**
     * Load available games from store
     */
    private void loadStoreGames() {
        logger.info("Loading available games from store");

        storeGamesGrid.getChildren().clear();

        try {
            List<GameInfoDto> availableGames = gameManager.getUninstalledGames();

            if (availableGames.isEmpty()) {
                Label emptyLabel = new Label("All available games are already installed!\nCheck back later for new games.");
                emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #999; -fx-padding: 40; -fx-text-alignment: center;");
                storeGamesGrid.getChildren().add(emptyLabel);
                return;
            }

            // Create cards for each available game
            for (GameInfoDto game : availableGames) {
                VBox gameCard = createStoreGameCard(game);
                storeGamesGrid.getChildren().add(gameCard);
            }

            logger.info("Loaded {} available games from store", availableGames.size());

        } catch (Exception e) {
            logger.error("Error loading store games", e);
            Label errorLabel = new Label("Failed to load games from store.\nCheck your internet connection and try again.");
            errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f44336; -fx-padding: 40; -fx-text-alignment: center;");
            storeGamesGrid.getChildren().add(errorLabel);
        }
    }

    /**
     * Create a card for a store game
     */
    private VBox createStoreGameCard(GameInfoDto game) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(250);
        card.setMaxWidth(250);

        // Game name
        Label nameLabel = new Label(game.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(220);

        // Subject
        Label subjectLabel = new Label(game.getSubject());
        subjectLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2196F3; -fx-font-weight: bold;");

        // Description
        Label descLabel = new Label(game.getDescription());
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(220);
        descLabel.setMaxHeight(60);

        // Grade range
        Label gradeLabel = new Label(game.getGradeRange());
        gradeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");

        // File size
        Label sizeLabel = new Label("Size: " + game.getFormattedFileSize());
        sizeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");

        // Download button
        Button downloadBtn = new Button("⬇ Download");
        downloadBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; " +
                           "-fx-padding: 8px 20px; -fx-background-radius: 4px; -fx-cursor: hand;");
        downloadBtn.setMaxWidth(Double.MAX_VALUE);
        downloadBtn.setOnAction(e -> handleDownloadGame(game, downloadBtn));

        card.getChildren().addAll(nameLabel, subjectLabel, descLabel, gradeLabel, sizeLabel, downloadBtn);

        return card;
    }

    /**
     * Handle download game button
     */
    private void handleDownloadGame(GameInfoDto game, Button downloadBtn) {
        logger.info("Download requested for game: {}", game.getName());

        if (!networkMonitor.isOnline()) {
            showError("Cannot download: No network connection");
            return;
        }

        // Disable button and show progress
        downloadBtn.setDisable(true);
        downloadBtn.setText("Downloading...");

        // Download in background
        new Thread(() -> {
            try {
                gameManager.downloadAndInstallGame(game.getGameId(), (message, percentage) -> {
                    Platform.runLater(() -> {
                        downloadBtn.setText(percentage + "%");
                        showInfo(message);
                    });
                });

                // Success - refresh both tabs
                Platform.runLater(() -> {
                    showInfo("Game installed successfully: " + game.getName());
                    loadGames();
                    loadStoreGames();
                });

            } catch (Exception e) {
                logger.error("Failed to download game: " + game.getName(), e);
                Platform.runLater(() -> {
                    showError("Download failed: " + e.getMessage());
                    downloadBtn.setDisable(false);
                    downloadBtn.setText("⬇ Download");
                });
            }
        }).start();
    }

    /**
     * Handle refresh button
     */
    @FXML
    public void handleRefresh() {
        logger.info("Refreshing games list");
        loadGames();
        updateSyncStatus();
        showInfo("Games refreshed");
    }

    /**
     * Handle refresh store button
     */
    @FXML
    public void handleRefreshStore() {
        logger.info("Refreshing game store");

        if (!networkMonitor.isOnline()) {
            showError("Cannot refresh store: No network connection");
            return;
        }

        loadStoreGames();
        showInfo("Store refreshed");
    }

    /**
     * Handle sync now button
     */
    @FXML
    public void handleSyncNow() {
        logger.info("Manual sync requested");

        if (!networkMonitor.isOnline()) {
            showError("Cannot sync: No network connection");
            return;
        }

        if (syncService.isSyncing()) {
            showInfo("Sync already in progress");
            return;
        }

        // Perform sync in background thread
        new Thread(() -> {
            Platform.runLater(() -> showInfo("Syncing scores..."));
            syncService.performSync();
            Platform.runLater(() -> {
                updateSyncStatus();
                showInfo(syncService.getLastSyncMessage());
            });
        }).start();
    }

    /**
     * Handle settings button
     */
    @FXML
    public void handleSettings() {
        logger.info("Settings requested");

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/view/settings.fxml")
            );
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 600, 500);

            // Pass dependencies to controller
            com.heronix.edu.client.ui.controller.SettingsController controller = loader.getController();
            javafx.stage.Stage settingsStage = new javafx.stage.Stage();
            controller.initialize(deviceService, syncService, settingsStage);

            settingsStage.setScene(scene);
            settingsStage.setTitle("Settings");
            settingsStage.initOwner(stage);
            settingsStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            settingsStage.show();

        } catch (Exception e) {
            logger.error("Failed to open settings", e);
            showError("Failed to open settings: " + e.getMessage());
        }
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
