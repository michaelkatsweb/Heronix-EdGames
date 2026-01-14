package com.heronix.edu.games.crossword.ui;

import com.heronix.edu.games.crossword.model.*;
import javafx.geometry.Insets;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Grid view for the crossword puzzle.
 * Manages cell views and keyboard navigation.
 */
public class CrosswordGridView extends GridPane {

    private final CrosswordPuzzle puzzle;
    private final CrosswordCellView[][] cellViews;

    private CrosswordCellView selectedCell;
    private Direction currentDirection = Direction.ACROSS;

    private Consumer<CrosswordWord> onWordComplete;
    private BiConsumer<Integer, Integer> onCellChange;
    private Consumer<CrosswordClue> onClueSelect;

    public CrosswordGridView(CrosswordPuzzle puzzle) {
        this.puzzle = puzzle;
        this.cellViews = new CrosswordCellView[puzzle.getGridHeight()][puzzle.getGridWidth()];

        setupGrid();
        setupStyle();
    }

    private void setupGrid() {
        setHgap(1);
        setVgap(1);
        setPadding(new Insets(10));

        CrosswordCell[][] grid = puzzle.getGrid();

        for (int row = 0; row < puzzle.getGridHeight(); row++) {
            for (int col = 0; col < puzzle.getGridWidth(); col++) {
                CrosswordCellView cellView = new CrosswordCellView(grid[row][col]);
                cellViews[row][col] = cellView;

                // Setup event handlers
                final int r = row;
                final int c = col;

                cellView.setOnSelect(cv -> handleCellSelect(r, c));
                cellView.setOnInput(ch -> handleInput(r, c, ch));
                cellView.setOnNavigate(event -> handleNavigation(r, c, event));

                add(cellView, col, row);
            }
        }
    }

    private void setupStyle() {
        setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");
    }

    /**
     * Handle cell selection.
     */
    private void handleCellSelect(int row, int col) {
        // If clicking same cell, toggle direction
        if (selectedCell != null && selectedCell.getRow() == row && selectedCell.getCol() == col) {
            toggleDirection();
        }

        selectCell(row, col);
    }

    /**
     * Handle character input.
     */
    private void handleInput(int row, int col, char ch) {
        if (onCellChange != null) {
            onCellChange.accept(row, col);
        }

        // Check if word is complete
        CrosswordWord word = puzzle.getWordAt(row, col, currentDirection);
        if (word != null && puzzle.isWordComplete(word)) {
            puzzle.lockWordIfComplete(word);
            updateWordCells(word);
            if (onWordComplete != null) {
                onWordComplete.accept(word);
            }
        }

        // Auto-advance to next cell
        if (ch != '\0') {
            moveToNextCell();
        }
    }

    /**
     * Handle keyboard navigation.
     */
    private void handleNavigation(int row, int col, KeyEvent event) {
        KeyCode code = event.getCode();

        switch (code) {
            case UP:
                moveSelection(row - 1, col);
                break;
            case DOWN:
                moveSelection(row + 1, col);
                break;
            case LEFT:
                moveSelection(row, col - 1);
                break;
            case RIGHT:
                moveSelection(row, col + 1);
                break;
            case TAB:
                if (event.isShiftDown()) {
                    moveToPreviousWord();
                } else {
                    moveToNextWord();
                }
                break;
            case ENTER:
                toggleDirection();
                break;
            case BACK_SPACE:
                moveToPreviousCell();
                break;
            default:
                // Ignore other keys
                break;
        }
    }

    /**
     * Select a cell and highlight its word.
     */
    public void selectCell(int row, int col) {
        // Clear previous selection
        if (selectedCell != null) {
            selectedCell.setSelected(false);
            clearHighlight();
        }

        CrosswordCell cell = puzzle.getCell(row, col);
        if (cell == null || cell.isBlocked()) {
            return;
        }

        selectedCell = cellViews[row][col];
        selectedCell.setSelected(true);
        selectedCell.focus();

        // Find and highlight current word
        CrosswordWord word = puzzle.getWordAt(row, col, currentDirection);
        if (word == null) {
            // Try other direction
            currentDirection = currentDirection.opposite();
            word = puzzle.getWordAt(row, col, currentDirection);
        }

        if (word != null) {
            highlightWord(word);
            notifyClueSelect(word);
        }
    }

    /**
     * Highlight all cells in a word.
     */
    private void highlightWord(CrosswordWord word) {
        for (int i = 0; i < word.getLength(); i++) {
            int r = word.getRow() + (word.getDirection() == Direction.DOWN ? i : 0);
            int c = word.getCol() + (word.getDirection() == Direction.ACROSS ? i : 0);

            if (puzzle.isValidPosition(r, c)) {
                CrosswordCellView cv = cellViews[r][c];
                if (cv != selectedCell) {
                    cv.setHighlighted(true);
                }
            }
        }
    }

    /**
     * Clear all highlights.
     */
    private void clearHighlight() {
        for (int r = 0; r < puzzle.getGridHeight(); r++) {
            for (int c = 0; c < puzzle.getGridWidth(); c++) {
                cellViews[r][c].setHighlighted(false);
            }
        }
    }

    /**
     * Update cell views after word completion.
     */
    private void updateWordCells(CrosswordWord word) {
        for (int i = 0; i < word.getLength(); i++) {
            int r = word.getRow() + (word.getDirection() == Direction.DOWN ? i : 0);
            int c = word.getCol() + (word.getDirection() == Direction.ACROSS ? i : 0);

            if (puzzle.isValidPosition(r, c)) {
                cellViews[r][c].updateView();
            }
        }
    }

    /**
     * Toggle between ACROSS and DOWN direction.
     */
    public void toggleDirection() {
        currentDirection = currentDirection.opposite();
        if (selectedCell != null) {
            clearHighlight();
            selectCell(selectedCell.getRow(), selectedCell.getCol());
        }
    }

    /**
     * Move to the next cell in current direction.
     */
    private void moveToNextCell() {
        if (selectedCell == null) return;

        int newRow = selectedCell.getRow() + currentDirection.getRowOffset();
        int newCol = selectedCell.getCol() + currentDirection.getColOffset();

        moveSelection(newRow, newCol);
    }

    /**
     * Move to the previous cell in current direction.
     */
    private void moveToPreviousCell() {
        if (selectedCell == null) return;

        int newRow = selectedCell.getRow() - currentDirection.getRowOffset();
        int newCol = selectedCell.getCol() - currentDirection.getColOffset();

        moveSelection(newRow, newCol);
    }

    /**
     * Move selection to a position if valid.
     */
    private void moveSelection(int row, int col) {
        CrosswordCell cell = puzzle.getCell(row, col);
        if (cell != null && !cell.isBlocked()) {
            selectCell(row, col);
        }
    }

    /**
     * Move to the next word.
     */
    private void moveToNextWord() {
        if (selectedCell == null) return;

        CrosswordWord currentWord = puzzle.getWordAt(selectedCell.getRow(), selectedCell.getCol(), currentDirection);
        if (currentWord == null) return;

        // Find next word in same direction
        var clues = currentDirection == Direction.ACROSS ? puzzle.getAcrossClues() : puzzle.getDownClues();
        boolean foundCurrent = false;

        for (CrosswordClue clue : clues) {
            if (foundCurrent && clue.getWordRef() != null) {
                CrosswordWord next = clue.getWordRef();
                selectCell(next.getRow(), next.getCol());
                return;
            }
            if (clue.getWordRef() == currentWord) {
                foundCurrent = true;
            }
        }

        // Wrap to first word or switch direction
        if (!clues.isEmpty() && clues.get(0).getWordRef() != null) {
            CrosswordWord first = clues.get(0).getWordRef();
            selectCell(first.getRow(), first.getCol());
        }
    }

    /**
     * Move to the previous word.
     */
    private void moveToPreviousWord() {
        if (selectedCell == null) return;

        CrosswordWord currentWord = puzzle.getWordAt(selectedCell.getRow(), selectedCell.getCol(), currentDirection);
        if (currentWord == null) return;

        var clues = currentDirection == Direction.ACROSS ? puzzle.getAcrossClues() : puzzle.getDownClues();
        CrosswordWord previous = null;

        for (CrosswordClue clue : clues) {
            if (clue.getWordRef() == currentWord && previous != null) {
                selectCell(previous.getRow(), previous.getCol());
                return;
            }
            previous = clue.getWordRef();
        }

        // Wrap to last word
        if (!clues.isEmpty()) {
            CrosswordWord last = clues.get(clues.size() - 1).getWordRef();
            if (last != null) {
                selectCell(last.getRow(), last.getCol());
            }
        }
    }

    /**
     * Navigate to a specific word (called from clue list).
     */
    public void navigateToWord(CrosswordWord word) {
        currentDirection = word.getDirection();
        selectCell(word.getRow(), word.getCol());
    }

    /**
     * Notify clue selection callback.
     */
    private void notifyClueSelect(CrosswordWord word) {
        if (onClueSelect != null) {
            var clues = word.getDirection() == Direction.ACROSS ? puzzle.getAcrossClues() : puzzle.getDownClues();
            for (CrosswordClue clue : clues) {
                if (clue.getWordRef() == word) {
                    onClueSelect.accept(clue);
                    return;
                }
            }
        }
    }

    /**
     * Refresh all cell views.
     */
    public void refreshAll() {
        for (int r = 0; r < puzzle.getGridHeight(); r++) {
            for (int c = 0; c < puzzle.getGridWidth(); c++) {
                cellViews[r][c].updateView();
            }
        }
    }

    /**
     * Show error feedback on a cell.
     */
    public void showCellError(int row, int col) {
        if (puzzle.isValidPosition(row, col)) {
            cellViews[row][col].showError();
        }
    }

    /**
     * Show correct feedback on a cell.
     */
    public void showCellCorrect(int row, int col) {
        if (puzzle.isValidPosition(row, col)) {
            cellViews[row][col].showCorrect();
        }
    }

    // Event handler setters

    public void setOnWordComplete(Consumer<CrosswordWord> handler) {
        this.onWordComplete = handler;
    }

    public void setOnCellChange(BiConsumer<Integer, Integer> handler) {
        this.onCellChange = handler;
    }

    public void setOnClueSelect(Consumer<CrosswordClue> handler) {
        this.onClueSelect = handler;
    }

    // Getters

    public CrosswordPuzzle getPuzzle() {
        return puzzle;
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public CrosswordCellView getSelectedCell() {
        return selectedCell;
    }
}
