package com.heronix.edu.client.ui.component;

import com.heronix.edu.client.db.entity.InstalledGame;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

/**
 * Visual component representing an installed game
 * Displays game info and provides a launch button
 */
public class GameCard extends VBox {

    private final InstalledGame game;
    private Button playButton;
    private Button uninstallButton;

    public GameCard(InstalledGame game) {
        this.game = game;
        setupUI();
    }

    private void setupUI() {
        // Card styling
        setAlignment(Pos.CENTER);
        setPrefSize(200, 250);
        setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 8px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
                "-fx-padding: 20; " +
                "-fx-spacing: 15;");

        // Game icon placeholder (could be replaced with actual icon)
        Label iconLabel = new Label("🎮");
        iconLabel.setStyle("-fx-font-size: 48px;");

        // Game name
        Label nameLabel = new Label(game.getGameName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(180);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setStyle("-fx-font-size: 16px; " +
                          "-fx-font-weight: bold; " +
                          "-fx-text-fill: #333;");

        // Game subject
        Label subjectLabel = new Label(game.getSubject() != null ? game.getSubject() : "Education");
        subjectLabel.setStyle("-fx-font-size: 12px; " +
                             "-fx-text-fill: #666; " +
                             "-fx-background-color: #E3F2FD; " +
                             "-fx-padding: 4px 12px; " +
                             "-fx-background-radius: 12px;");

        // Last played info
        Label lastPlayedLabel = new Label(getLastPlayedText());
        lastPlayedLabel.setStyle("-fx-font-size: 11px; " +
                                "-fx-text-fill: #999;");

        // Play button
        playButton = new Button("Play");
        playButton.setPrefWidth(160);
        playButton.setStyle("-fx-background-color: #4CAF50; " +
                           "-fx-text-fill: white; " +
                           "-fx-font-size: 14px; " +
                           "-fx-font-weight: bold; " +
                           "-fx-padding: 10px 20px; " +
                           "-fx-background-radius: 4px; " +
                           "-fx-cursor: hand;");

        // Hover effect
        playButton.setOnMouseEntered(e ->
            playButton.setStyle("-fx-background-color: #45A049; " +
                               "-fx-text-fill: white; " +
                               "-fx-font-size: 14px; " +
                               "-fx-font-weight: bold; " +
                               "-fx-padding: 10px 20px; " +
                               "-fx-background-radius: 4px; " +
                               "-fx-cursor: hand;"));

        playButton.setOnMouseExited(e ->
            playButton.setStyle("-fx-background-color: #4CAF50; " +
                               "-fx-text-fill: white; " +
                               "-fx-font-size: 14px; " +
                               "-fx-font-weight: bold; " +
                               "-fx-padding: 10px 20px; " +
                               "-fx-background-radius: 4px; " +
                               "-fx-cursor: hand;"));

        // Uninstall button
        uninstallButton = new Button("Uninstall");
        uninstallButton.setPrefWidth(160);
        uninstallButton.setStyle("-fx-background-color: #f44336; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 12px; " +
                                "-fx-padding: 8px 16px; " +
                                "-fx-background-radius: 4px; " +
                                "-fx-cursor: hand;");

        // Hover effect for uninstall button
        uninstallButton.setOnMouseEntered(e ->
            uninstallButton.setStyle("-fx-background-color: #da190b; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-font-size: 12px; " +
                                    "-fx-padding: 8px 16px; " +
                                    "-fx-background-radius: 4px; " +
                                    "-fx-cursor: hand;"));

        uninstallButton.setOnMouseExited(e ->
            uninstallButton.setStyle("-fx-background-color: #f44336; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-font-size: 12px; " +
                                    "-fx-padding: 8px 16px; " +
                                    "-fx-background-radius: 4px; " +
                                    "-fx-cursor: hand;"));

        // Add spacing
        VBox.setMargin(playButton, new Insets(10, 0, 0, 0));
        VBox.setMargin(uninstallButton, new Insets(5, 0, 0, 0));

        // Add all components
        getChildren().addAll(iconLabel, nameLabel, subjectLabel, lastPlayedLabel, playButton, uninstallButton);
    }

    /**
     * Get last played text
     */
    private String getLastPlayedText() {
        if (game.getLastPlayedAt() == null) {
            return "Never played";
        }

        // Simple relative time
        java.time.Duration duration = java.time.Duration.between(
            game.getLastPlayedAt(),
            java.time.LocalDateTime.now()
        );

        long days = duration.toDays();
        if (days == 0) {
            return "Played today";
        } else if (days == 1) {
            return "Played yesterday";
        } else if (days < 7) {
            return "Played " + days + " days ago";
        } else {
            return "Played " + (days / 7) + " weeks ago";
        }
    }

    /**
     * Set the play button action
     */
    public void setOnPlay(Runnable action) {
        playButton.setOnAction(e -> action.run());
    }

    /**
     * Set the uninstall button action
     */
    public void setOnUninstall(Runnable action) {
        uninstallButton.setOnAction(e -> action.run());
    }

    /**
     * Get the game entity
     */
    public InstalledGame getGame() {
        return game;
    }
}
