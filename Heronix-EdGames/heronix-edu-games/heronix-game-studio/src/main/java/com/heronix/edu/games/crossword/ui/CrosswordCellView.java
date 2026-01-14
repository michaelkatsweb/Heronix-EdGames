package com.heronix.edu.games.crossword.ui;

import com.heronix.edu.games.crossword.model.CrosswordCell;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.function.Consumer;

/**
 * Visual component for a single crossword cell.
 * Handles input, display states, and clue numbers.
 */
public class CrosswordCellView extends StackPane {

    private static final double CELL_SIZE = 40;
    private static final Color BLOCKED_COLOR = Color.rgb(40, 40, 40);
    private static final Color EMPTY_COLOR = Color.WHITE;
    private static final Color SELECTED_COLOR = Color.rgb(255, 255, 180);
    private static final Color HIGHLIGHTED_COLOR = Color.rgb(220, 240, 255);
    private static final Color CORRECT_COLOR = Color.rgb(200, 255, 200);
    private static final Color INCORRECT_COLOR = Color.rgb(255, 200, 200);
    private static final Color LOCKED_COLOR = Color.rgb(180, 255, 180);

    private final CrosswordCell cell;
    private final Rectangle background;
    private final TextField inputField;
    private final Text clueNumberText;

    private boolean selected;
    private boolean highlighted;

    private Consumer<CrosswordCellView> onSelect;
    private Consumer<Character> onInput;
    private Consumer<KeyEvent> onNavigate;

    public CrosswordCellView(CrosswordCell cell) {
        this.cell = cell;

        // Background rectangle
        background = new Rectangle(CELL_SIZE, CELL_SIZE);
        background.setStroke(Color.rgb(100, 100, 100));
        background.setStrokeWidth(1);

        // Clue number (small text in corner)
        clueNumberText = new Text();
        clueNumberText.setFont(Font.font("Arial", FontWeight.NORMAL, 9));
        clueNumberText.setFill(Color.rgb(80, 80, 80));
        StackPane.setAlignment(clueNumberText, Pos.TOP_LEFT);

        // Input field
        inputField = new TextField();
        inputField.setPrefSize(CELL_SIZE - 4, CELL_SIZE - 4);
        inputField.setMaxSize(CELL_SIZE - 4, CELL_SIZE - 4);
        inputField.setAlignment(Pos.CENTER);
        inputField.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        inputField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;");

        // Setup input handling
        setupInputHandling();

        // Add children
        getChildren().addAll(background, clueNumberText, inputField);

        // Initial update
        updateView();
    }

    private void setupInputHandling() {
        // Focus handling
        inputField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal && !cell.isBlocked()) {
                setSelected(true);
                if (onSelect != null) {
                    onSelect.accept(this);
                }
            }
        });

        // Key press handling
        inputField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (cell.isBlocked() || cell.isLocked()) {
                event.consume();
                return;
            }

            switch (event.getCode()) {
                case BACK_SPACE:
                case DELETE:
                    // Clear cell
                    cell.setUserEntry('\0');
                    inputField.clear();
                    if (onInput != null) {
                        onInput.accept('\0');
                    }
                    event.consume();
                    break;

                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                case TAB:
                case ENTER:
                    // Navigation - pass to handler
                    if (onNavigate != null) {
                        onNavigate.accept(event);
                    }
                    event.consume();
                    break;

                default:
                    // Letter input
                    String text = event.getText();
                    if (text != null && text.length() == 1) {
                        char c = text.toUpperCase().charAt(0);
                        if (Character.isLetter(c)) {
                            cell.setUserEntry(c);
                            inputField.setText(String.valueOf(c));
                            if (onInput != null) {
                                onInput.accept(c);
                            }
                        }
                    }
                    event.consume();
                    break;
            }
        });

        // Block text input changes from typing (we handle it in key pressed)
        inputField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 1) {
                inputField.setText(newVal.substring(0, 1).toUpperCase());
            }
        });

        // Click handling
        setOnMouseClicked(event -> {
            if (!cell.isBlocked()) {
                inputField.requestFocus();
            }
        });
    }

    /**
     * Update the visual state of the cell.
     */
    public void updateView() {
        if (cell.isBlocked()) {
            background.setFill(BLOCKED_COLOR);
            inputField.setVisible(false);
            clueNumberText.setVisible(false);
            return;
        }

        inputField.setVisible(true);

        // Update background color
        if (cell.isLocked()) {
            background.setFill(LOCKED_COLOR);
        } else if (selected) {
            background.setFill(SELECTED_COLOR);
        } else if (highlighted) {
            background.setFill(HIGHLIGHTED_COLOR);
        } else if (cell.isRevealed()) {
            background.setFill(CORRECT_COLOR);
        } else {
            background.setFill(EMPTY_COLOR);
        }

        // Update text
        if (cell.isFilled()) {
            inputField.setText(String.valueOf(cell.getUserEntry()));
        } else {
            inputField.clear();
        }

        // Update clue number
        int clueNum = cell.getAcrossClueNum();
        if (clueNum == 0) {
            clueNum = cell.getDownClueNum();
        }
        if (clueNum > 0) {
            clueNumberText.setText(String.valueOf(clueNum));
            clueNumberText.setVisible(true);
        } else {
            clueNumberText.setVisible(false);
        }

        // Disable input if locked
        inputField.setEditable(!cell.isLocked());
    }

    /**
     * Mark this cell as having an error (shake animation could be added).
     */
    public void showError() {
        background.setFill(INCORRECT_COLOR);
    }

    /**
     * Mark this cell as correct.
     */
    public void showCorrect() {
        background.setFill(CORRECT_COLOR);
    }

    // Selection and highlighting

    public void setSelected(boolean selected) {
        this.selected = selected;
        updateView();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
        updateView();
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    // Cell access

    public CrosswordCell getCell() {
        return cell;
    }

    public int getRow() {
        return cell.getRow();
    }

    public int getCol() {
        return cell.getCol();
    }

    // Event handlers

    public void setOnSelect(Consumer<CrosswordCellView> onSelect) {
        this.onSelect = onSelect;
    }

    public void setOnInput(Consumer<Character> onInput) {
        this.onInput = onInput;
    }

    public void setOnNavigate(Consumer<KeyEvent> onNavigate) {
        this.onNavigate = onNavigate;
    }

    /**
     * Focus this cell's input field.
     */
    public void focus() {
        if (!cell.isBlocked()) {
            inputField.requestFocus();
        }
    }
}
