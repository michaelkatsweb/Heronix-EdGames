package com.heronix.edu.client.ui.controller;

import com.heronix.edu.client.db.entity.SyncConflict;
import com.heronix.edu.client.db.entity.SyncConflict.Resolution;
import com.heronix.edu.client.service.SyncService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the Sync Conflicts resolution dialog.
 * Allows users to view and resolve data conflicts between local and server.
 */
public class SyncConflictsController {
    private static final Logger logger = LoggerFactory.getLogger(SyncConflictsController.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML private ListView<SyncConflict> conflictListView;
    @FXML private Label conflictCountLabel;

    // Detail panel
    @FXML private VBox detailPanel;
    @FXML private Label entityTypeLabel;
    @FXML private Label entityIdLabel;
    @FXML private Label conflictTypeLabel;
    @FXML private TextArea localValueArea;
    @FXML private Label localTimestampLabel;
    @FXML private TextArea serverValueArea;
    @FXML private Label serverTimestampLabel;

    // Buttons
    @FXML private Button keepLocalButton;
    @FXML private Button keepServerButton;
    @FXML private Button autoResolveAllButton;
    @FXML private Button refreshButton;
    @FXML private Button closeButton;
    @FXML private Label statusLabel;

    private SyncService syncService;
    private Stage stage;
    private Runnable onConflictsResolved;
    private ObservableList<SyncConflict> conflictList = FXCollections.observableArrayList();
    private SyncConflict selectedConflict;

    /**
     * Initialize the controller with dependencies
     */
    public void initialize(SyncService syncService, Stage stage, Runnable onConflictsResolved) {
        this.syncService = syncService;
        this.stage = stage;
        this.onConflictsResolved = onConflictsResolved;

        setupListView();
        loadConflicts();
    }

    @FXML
    public void initialize() {
        // FXML initialization - called before manual initialize
    }

    private void setupListView() {
        conflictListView.setItems(conflictList);

        // Custom cell factory for displaying conflicts
        conflictListView.setCellFactory(listView -> new ListCell<SyncConflict>() {
            @Override
            protected void updateItem(SyncConflict conflict, boolean empty) {
                super.updateItem(conflict, empty);

                if (empty || conflict == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    String text = String.format("%s: %s (%s)",
                        formatEntityType(conflict.getEntityType()),
                        truncateId(conflict.getEntityId()),
                        conflict.getConflictType().name().replace("_", " "));

                    setText(text);

                    // Color based on conflict type
                    switch (conflict.getConflictType()) {
                        case UPDATE_CONFLICT:
                            setStyle("-fx-text-fill: #E65100;");
                            break;
                        case VERSION_MISMATCH:
                            setStyle("-fx-text-fill: #1565C0;");
                            break;
                        case DELETE_CONFLICT:
                            setStyle("-fx-text-fill: #C62828;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        // Selection listener
        conflictListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    showConflictDetails(newVal);
                } else {
                    hideConflictDetails();
                }
            }
        );
    }

    private void loadConflicts() {
        List<SyncConflict> conflicts = syncService.getPendingConflicts();
        conflictList.setAll(conflicts);
        updateConflictCount();

        if (conflicts.isEmpty()) {
            statusLabel.setText("No conflicts to resolve");
            autoResolveAllButton.setDisable(true);
        } else {
            statusLabel.setText("");
            autoResolveAllButton.setDisable(false);
        }
    }

    private void updateConflictCount() {
        int count = conflictList.size();
        conflictCountLabel.setText(count + " conflict" + (count != 1 ? "s" : "") + " pending");
    }

    private void showConflictDetails(SyncConflict conflict) {
        selectedConflict = conflict;

        entityTypeLabel.setText(formatEntityType(conflict.getEntityType()));
        entityIdLabel.setText(conflict.getEntityId());
        conflictTypeLabel.setText(conflict.getConflictType().name().replace("_", " "));

        localValueArea.setText(conflict.getLocalValue() != null ? conflict.getLocalValue() : "N/A");
        localTimestampLabel.setText(conflict.getLocalTimestamp() != null ?
            "Modified: " + conflict.getLocalTimestamp().format(DATE_FORMAT) : "");

        serverValueArea.setText(conflict.getServerValue() != null ? conflict.getServerValue() : "N/A");
        serverTimestampLabel.setText(conflict.getServerTimestamp() != null ?
            "Modified: " + conflict.getServerTimestamp().format(DATE_FORMAT) : "");

        // Show detail panel
        detailPanel.setVisible(true);
        detailPanel.setManaged(true);

        // Enable resolution buttons
        keepLocalButton.setDisable(false);
        keepServerButton.setDisable(false);
    }

    private void hideConflictDetails() {
        selectedConflict = null;
        detailPanel.setVisible(false);
        detailPanel.setManaged(false);
    }

    @FXML
    private void handleKeepLocal() {
        if (selectedConflict == null) return;

        resolveConflict(selectedConflict, Resolution.KEEP_LOCAL);
    }

    @FXML
    private void handleKeepServer() {
        if (selectedConflict == null) return;

        resolveConflict(selectedConflict, Resolution.KEEP_SERVER);
    }

    private void resolveConflict(SyncConflict conflict, Resolution resolution) {
        try {
            syncService.resolveConflict(conflict.getId(), resolution);

            // Remove from list
            conflictList.remove(conflict);
            hideConflictDetails();
            updateConflictCount();

            statusLabel.setText("Conflict resolved: " + resolution.name());
            logger.info("Resolved conflict {} as {}", conflict.getId(), resolution);

            // Check if all conflicts resolved
            checkAllResolved();

        } catch (Exception e) {
            logger.error("Error resolving conflict", e);
            showError("Failed to resolve conflict: " + e.getMessage());
        }
    }

    @FXML
    private void handleAutoResolveAll() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Auto-Resolve All Conflicts");
        confirm.setHeaderText("Resolve all conflicts using Server Wins strategy?");
        confirm.setContentText("This will discard all local changes and keep the server version for all " +
            conflictList.size() + " conflicts. This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int resolved = syncService.autoResolveAllConflicts();
                loadConflicts();
                statusLabel.setText("Auto-resolved " + resolved + " conflicts");

                checkAllResolved();

            } catch (Exception e) {
                logger.error("Error auto-resolving conflicts", e);
                showError("Failed to auto-resolve: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadConflicts();
        hideConflictDetails();
        statusLabel.setText("Refreshed conflict list");
    }

    @FXML
    private void handleClose() {
        if (stage != null) {
            stage.close();
        }
    }

    private void checkAllResolved() {
        if (conflictList.isEmpty()) {
            statusLabel.setText("All conflicts resolved!");
            autoResolveAllButton.setDisable(true);

            // Notify caller
            if (onConflictsResolved != null) {
                Platform.runLater(onConflictsResolved);
            }
        }
    }

    private String formatEntityType(String entityType) {
        if (entityType == null) return "Unknown";

        return switch (entityType) {
            case "game_score" -> "Game Score";
            case "student" -> "Student";
            case "device" -> "Device";
            default -> entityType.replace("_", " ");
        };
    }

    private String truncateId(String id) {
        if (id == null) return "";
        if (id.length() <= 12) return id;
        return id.substring(0, 8) + "..." + id.substring(id.length() - 4);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Static method to show the conflicts dialog
     */
    public static void showConflictsDialog(SyncService syncService, Stage owner, Runnable onResolved) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                SyncConflictsController.class.getResource("/view/sync-conflicts.fxml")
            );
            javafx.scene.Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Sync Conflicts");
            dialogStage.initOwner(owner);
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            dialogStage.setScene(scene);

            SyncConflictsController controller = loader.getController();
            controller.initialize(syncService, dialogStage, onResolved);

            dialogStage.showAndWait();

        } catch (Exception e) {
            logger.error("Failed to show conflicts dialog", e);
        }
    }
}
