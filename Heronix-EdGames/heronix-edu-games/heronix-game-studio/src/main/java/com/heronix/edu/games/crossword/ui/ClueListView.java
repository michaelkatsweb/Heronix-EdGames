package com.heronix.edu.games.crossword.ui;

import com.heronix.edu.games.crossword.model.CrosswordClue;
import com.heronix.edu.games.crossword.model.CrosswordWord;
import com.heronix.edu.games.crossword.model.Direction;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Panel displaying crossword clues (Across and Down sections).
 */
public class ClueListView extends VBox {

    private final VBox acrossBox;
    private final VBox downBox;
    private final Map<CrosswordClue, Label> clueLabels;

    private CrosswordClue selectedClue;
    private Consumer<CrosswordWord> onClueClick;

    public ClueListView() {
        this.clueLabels = new HashMap<>();

        setPadding(new Insets(10));
        setSpacing(15);
        setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 5;");
        setPrefWidth(280);

        // Across section
        Label acrossHeader = createSectionHeader("ACROSS");
        acrossBox = new VBox(5);
        acrossBox.setPadding(new Insets(5, 0, 0, 10));
        acrossBox.setStyle("-fx-background-color: #FFFFFF;");

        ScrollPane acrossScroll = new ScrollPane(acrossBox);
        acrossScroll.setFitToWidth(true);
        acrossScroll.setPrefHeight(200);
        acrossScroll.setStyle("-fx-background: #FFFFFF; -fx-background-color: #FFFFFF;");

        // Down section
        Label downHeader = createSectionHeader("DOWN");
        downBox = new VBox(5);
        downBox.setPadding(new Insets(5, 0, 0, 10));
        downBox.setStyle("-fx-background-color: #FFFFFF;");

        ScrollPane downScroll = new ScrollPane(downBox);
        downScroll.setFitToWidth(true);
        downScroll.setPrefHeight(200);
        downScroll.setStyle("-fx-background: #FFFFFF; -fx-background-color: #FFFFFF;");

        getChildren().addAll(acrossHeader, acrossScroll, downHeader, downScroll);
    }

    private Label createSectionHeader(String text) {
        Label header = new Label(text);
        header.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        header.setStyle("-fx-border-color: #999999; -fx-border-width: 0 0 2 0; -fx-padding: 0 0 5 0; -fx-text-fill: #000000;");
        return header;
    }

    /**
     * Set the clues to display.
     */
    public void setClues(List<CrosswordClue> acrossClues, List<CrosswordClue> downClues) {
        acrossBox.getChildren().clear();
        downBox.getChildren().clear();
        clueLabels.clear();

        for (CrosswordClue clue : acrossClues) {
            Label label = createClueLabel(clue);
            acrossBox.getChildren().add(label);
            clueLabels.put(clue, label);
        }

        for (CrosswordClue clue : downClues) {
            Label label = createClueLabel(clue);
            downBox.getChildren().add(label);
            clueLabels.put(clue, label);
        }
    }

    private Label createClueLabel(CrosswordClue clue) {
        Label label = new Label(clue.getFormattedClue());
        label.setFont(Font.font("Arial", 12));
        label.setWrapText(true);
        label.setMaxWidth(240);
        label.setPadding(new Insets(3, 5, 3, 5));

        updateClueStyle(label, clue, false);

        // Click handler
        label.setOnMouseClicked(event -> {
            if (onClueClick != null && clue.getWordRef() != null) {
                onClueClick.accept(clue.getWordRef());
            }
        });

        // Hover effect
        label.setOnMouseEntered(event -> {
            if (!clue.isSolved() && clue != selectedClue) {
                label.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 3; -fx-cursor: hand; -fx-text-fill: #000000;");
            }
        });

        label.setOnMouseExited(event -> {
            updateClueStyle(label, clue, clue == selectedClue);
        });

        return label;
    }

    private void updateClueStyle(Label label, CrosswordClue clue, boolean selected) {
        if (clue.isSolved()) {
            label.setStyle("-fx-background-color: #d4edda; -fx-background-radius: 3; -fx-text-fill: #228B22;");
            label.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        } else if (selected) {
            label.setStyle("-fx-background-color: #fff3cd; -fx-background-radius: 3; -fx-text-fill: #000000;");
            label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        } else {
            label.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #000000;");
            label.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        }
    }

    /**
     * Select a clue (highlight it).
     */
    public void selectClue(CrosswordClue clue) {
        // Deselect previous
        if (selectedClue != null && clueLabels.containsKey(selectedClue)) {
            updateClueStyle(clueLabels.get(selectedClue), selectedClue, false);
        }

        selectedClue = clue;

        // Select new
        if (clue != null && clueLabels.containsKey(clue)) {
            Label label = clueLabels.get(clue);
            updateClueStyle(label, clue, true);

            // Scroll to visible
            scrollToClue(clue, label);
        }
    }

    private void scrollToClue(CrosswordClue clue, Label label) {
        // Get the scroll pane for this clue's direction
        ScrollPane scrollPane = clue.getWordRef() != null &&
                                clue.getWordRef().getDirection() == Direction.ACROSS ?
                                (ScrollPane) getChildren().get(1) : (ScrollPane) getChildren().get(3);

        // Calculate position to scroll to
        double labelY = label.getBoundsInParent().getMinY();
        double contentHeight = scrollPane.getContent().getBoundsInLocal().getHeight();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();

        if (contentHeight > viewportHeight) {
            double scrollValue = Math.max(0, Math.min(1, labelY / (contentHeight - viewportHeight)));
            scrollPane.setVvalue(scrollValue);
        }
    }

    /**
     * Mark a clue as solved.
     */
    public void markSolved(CrosswordClue clue) {
        clue.setSolved(true);
        if (clueLabels.containsKey(clue)) {
            updateClueStyle(clueLabels.get(clue), clue, clue == selectedClue);
        }
    }

    /**
     * Refresh all clue displays.
     */
    public void refresh() {
        for (Map.Entry<CrosswordClue, Label> entry : clueLabels.entrySet()) {
            updateClueStyle(entry.getValue(), entry.getKey(), entry.getKey() == selectedClue);
        }
    }

    /**
     * Set click handler for clues.
     */
    public void setOnClueClick(Consumer<CrosswordWord> handler) {
        this.onClueClick = handler;
    }

    /**
     * Get the currently selected clue.
     */
    public CrosswordClue getSelectedClue() {
        return selectedClue;
    }
}
