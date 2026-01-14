package com.heronix.edu.client;

import com.heronix.edu.client.api.HeronixApiClient;
import com.heronix.edu.client.db.DatabaseManager;
import com.heronix.edu.client.db.repository.DeviceRepository;
import com.heronix.edu.client.db.repository.GameScoreRepository;
import com.heronix.edu.client.db.repository.InstalledGameRepository;
import com.heronix.edu.client.security.TokenManager;
import com.heronix.edu.client.service.DeviceService;
import com.heronix.edu.client.service.GameManager;
import com.heronix.edu.client.service.ScoreService;
import com.heronix.edu.client.service.SyncService;
import com.heronix.edu.client.util.NetworkMonitor;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main JavaFX application for Heronix Educational Games Client
 */
public class HeronixClientApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(HeronixClientApplication.class);

    private DeviceService deviceService;
    private TokenManager tokenManager;
    private GameManager gameManager;
    private ScoreService scoreService;
    private SyncService syncService;
    private NetworkMonitor networkMonitor;

    @Override
    public void init() throws Exception {
        logger.info("Initializing Heronix Client Application");

        // Initialize database
        DatabaseManager.initialize();

        // Initialize repositories
        DeviceRepository deviceRepository = new DeviceRepository();
        InstalledGameRepository gameRepository = new InstalledGameRepository();
        GameScoreRepository scoreRepository = new GameScoreRepository();

        // Initialize services
        tokenManager = new TokenManager();
        HeronixApiClient apiClient = new HeronixApiClient(tokenManager);
        deviceService = new DeviceService(deviceRepository, tokenManager);
        gameManager = new GameManager(gameRepository, apiClient);
        scoreService = new ScoreService(scoreRepository, deviceService);

        // Initialize network monitor and sync service
        networkMonitor = NetworkMonitor.getInstance();
        syncService = new SyncService(scoreService, deviceService, apiClient, networkMonitor);

        logger.info("Application initialization complete");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info("Starting Heronix Client Application");

        primaryStage.setTitle("Heronix Educational Games");

        // Check device state and show appropriate screen
        if (!deviceService.isDeviceRegistered()) {
            // Not registered locally - check if registered on server (e.g., local DB was cleared)
            logger.info("Device not registered locally, checking server...");
            if (deviceService.syncDeviceFromServer()) {
                logger.info("Device found on server - synced locally");
                // Now proceed with normal flow
                handleRegisteredDevice(primaryStage);
            } else {
                logger.info("Device not registered on server either, showing setup wizard");
                showSetupWizard(primaryStage);
            }
        } else {
            // Device registered locally
            handleRegisteredDevice(primaryStage);
        }

        primaryStage.show();
    }

    /**
     * Handle flow for a device that is registered (either locally or synced from server)
     */
    private void handleRegisteredDevice(Stage primaryStage) throws Exception {
        if (!deviceService.isDeviceApproved()) {
            logger.info("Device registered but not approved, showing waiting screen");
            showWaitingForApproval(primaryStage);
        } else {
            logger.info("Device approved, authenticating and showing main launcher");

            // Try to authenticate to get token (non-blocking for offline mode)
            try {
                if (networkMonitor.isOnline()) {
                    deviceService.authenticateDevice();
                    logger.info("Device authenticated successfully");
                } else {
                    logger.info("Server offline, skipping authentication - using cached token if available");
                }
            } catch (Exception e) {
                logger.warn("Authentication failed (server may be offline): {}. Continuing in offline mode.", e.getMessage());
            }

            // Start background sync (will sync when server becomes available)
            syncService.startBackgroundSync();

            showMainLauncher(primaryStage);
        }
    }

    /**
     * Show setup wizard for device registration
     */
    private void showSetupWizard(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/setup-wizard.fxml"));
        Scene scene = new Scene(loader.load(), 600, 400);

        // Pass dependencies to controller
        com.heronix.edu.client.ui.controller.SetupWizardController controller = loader.getController();
        controller.initialize(deviceService, gameManager, scoreService, syncService, stage);

        stage.setScene(scene);
    }

    /**
     * Show waiting screen while device is pending approval
     */
    private void showWaitingForApproval(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/waiting-for-approval.fxml"));
        Scene scene = new Scene(loader.load(), 600, 300);

        // Pass dependencies to controller
        com.heronix.edu.client.ui.controller.WaitingForApprovalController controller = loader.getController();
        controller.initialize(deviceService, gameManager, scoreService, syncService, stage);

        stage.setScene(scene);
    }

    /**
     * Show main launcher
     */
    private void showMainLauncher(Stage stage) throws Exception {
        logger.info("Loading main launcher");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main-launcher.fxml"));
        Scene scene = new Scene(loader.load(), 1024, 768);

        // Pass dependencies to controller
        com.heronix.edu.client.ui.controller.MainLauncherController controller = loader.getController();
        controller.initialize(deviceService, gameManager, scoreService, syncService, stage);

        stage.setScene(scene);
        stage.setTitle("Heronix Educational Games");
    }

    @Override
    public void stop() throws Exception {
        logger.info("Shutting down Heronix Client Application");

        // Stop background services
        if (syncService != null) {
            syncService.stopBackgroundSync();
        }
        if (networkMonitor != null) {
            networkMonitor.stopMonitoring();
        }

        // Cleanup resources
        DatabaseManager.shutdown();

        logger.info("Application shutdown complete");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
