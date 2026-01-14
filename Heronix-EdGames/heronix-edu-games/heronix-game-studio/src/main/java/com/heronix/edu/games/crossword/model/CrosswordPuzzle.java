package com.heronix.edu.games.crossword.model;

import com.heronix.edu.common.game.DifficultyLevel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Complete crossword puzzle state.
 * Contains the grid, placed words, clues, and game progress.
 */
public class CrosswordPuzzle {
    private int gridWidth;
    private int gridHeight;
    private CrosswordCell[][] grid;
    private List<CrosswordWord> placedWords;
    private List<CrosswordClue> acrossClues;
    private List<CrosswordClue> downClues;
    private String subject;
    private DifficultyLevel difficulty;

    public CrosswordPuzzle(int width, int height) {
        this.gridWidth = width;
        this.gridHeight = height;
        this.grid = new CrosswordCell[height][width];
        this.placedWords = new ArrayList<>();
        this.acrossClues = new ArrayList<>();
        this.downClues = new ArrayList<>();

        // Initialize all cells as blocked
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                grid[r][c] = new CrosswordCell(r, c);
            }
        }
    }

    // Grid access

    public int getGridWidth() {
        return gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public CrosswordCell[][] getGrid() {
        return grid;
    }

    public CrosswordCell getCell(int row, int col) {
        if (row >= 0 && row < gridHeight && col >= 0 && col < gridWidth) {
            return grid[row][col];
        }
        return null;
    }

    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < gridHeight && col >= 0 && col < gridWidth;
    }

    // Words and clues

    public List<CrosswordWord> getPlacedWords() {
        return placedWords;
    }

    public void addWord(CrosswordWord word) {
        placedWords.add(word);

        // Place letters in grid
        for (int i = 0; i < word.getLength(); i++) {
            int r = word.getRow() + (word.getDirection() == Direction.DOWN ? i : 0);
            int c = word.getCol() + (word.getDirection() == Direction.ACROSS ? i : 0);

            if (isValidPosition(r, c)) {
                grid[r][c].setSolution(word.getWord().charAt(i));
            }
        }
    }

    public List<CrosswordClue> getAcrossClues() {
        return acrossClues;
    }

    public List<CrosswordClue> getDownClues() {
        return downClues;
    }

    public void setAcrossClues(List<CrosswordClue> acrossClues) {
        this.acrossClues = acrossClues;
    }

    public void setDownClues(List<CrosswordClue> downClues) {
        this.downClues = downClues;
    }

    // Metadata

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
    }

    // Progress tracking

    /**
     * Get the total number of cells that need to be filled.
     */
    public int getTotalCells() {
        int count = 0;
        for (int r = 0; r < gridHeight; r++) {
            for (int c = 0; c < gridWidth; c++) {
                if (!grid[r][c].isBlocked()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Get the number of correctly filled cells.
     */
    public int getSolvedCells() {
        int count = 0;
        for (int r = 0; r < gridHeight; r++) {
            for (int c = 0; c < gridWidth; c++) {
                if (grid[r][c].isCorrect()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Get the number of filled cells (correct or incorrect).
     */
    public int getFilledCells() {
        int count = 0;
        for (int r = 0; r < gridHeight; r++) {
            for (int c = 0; c < gridWidth; c++) {
                if (grid[r][c].isFilled()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Get completion percentage (0-100).
     */
    public double getCompletionPercentage() {
        int total = getTotalCells();
        return total > 0 ? (getSolvedCells() * 100.0) / total : 0;
    }

    /**
     * Check if the puzzle is completely solved.
     */
    public boolean isComplete() {
        return getSolvedCells() == getTotalCells();
    }

    /**
     * Get the number of completed words.
     */
    public int getCompletedWordCount() {
        int count = 0;
        for (CrosswordWord word : placedWords) {
            if (isWordComplete(word)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Check if a specific word is completely and correctly filled.
     */
    public boolean isWordComplete(CrosswordWord word) {
        for (int i = 0; i < word.getLength(); i++) {
            int r = word.getRow() + (word.getDirection() == Direction.DOWN ? i : 0);
            int c = word.getCol() + (word.getDirection() == Direction.ACROSS ? i : 0);

            CrosswordCell cell = getCell(r, c);
            if (cell == null || !cell.isCorrect()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Lock all cells in a word if the word is complete.
     */
    public void lockWordIfComplete(CrosswordWord word) {
        if (isWordComplete(word)) {
            for (int i = 0; i < word.getLength(); i++) {
                int r = word.getRow() + (word.getDirection() == Direction.DOWN ? i : 0);
                int c = word.getCol() + (word.getDirection() == Direction.ACROSS ? i : 0);

                CrosswordCell cell = getCell(r, c);
                if (cell != null) {
                    cell.setLocked(true);
                }
            }

            // Mark clue as solved
            List<CrosswordClue> clues = word.getDirection() == Direction.ACROSS ? acrossClues : downClues;
            for (CrosswordClue clue : clues) {
                if (clue.getWordRef() == word) {
                    clue.setSolved(true);
                    break;
                }
            }
        }
    }

    /**
     * Get the word at a specific position for a direction.
     */
    public CrosswordWord getWordAt(int row, int col, Direction direction) {
        for (CrosswordWord word : placedWords) {
            if (word.getDirection() == direction && word.containsPosition(row, col)) {
                return word;
            }
        }
        return null;
    }

    /**
     * Get all words that contain a specific cell.
     */
    public List<CrosswordWord> getWordsContaining(int row, int col) {
        List<CrosswordWord> result = new ArrayList<>();
        for (CrosswordWord word : placedWords) {
            if (word.containsPosition(row, col)) {
                result.add(word);
            }
        }
        return result;
    }

    /**
     * Build clue lists from placed words.
     * Should be called after all words are placed and numbered.
     */
    public void buildClueLists() {
        acrossClues.clear();
        downClues.clear();

        for (CrosswordWord word : placedWords) {
            CrosswordClue clue = CrosswordClue.fromWord(word);
            if (word.getDirection() == Direction.ACROSS) {
                acrossClues.add(clue);
            } else {
                downClues.add(clue);
            }
        }

        // Sort by clue number
        acrossClues.sort(Comparator.comparingInt(CrosswordClue::getNumber));
        downClues.sort(Comparator.comparingInt(CrosswordClue::getNumber));
    }

    /**
     * Reveal a specific cell (hint).
     */
    public void revealCell(int row, int col) {
        CrosswordCell cell = getCell(row, col);
        if (cell != null && !cell.isBlocked() && !cell.isLocked()) {
            cell.reveal();
        }
    }

    /**
     * Reveal an entire word (hint).
     */
    public void revealWord(CrosswordWord word) {
        for (int i = 0; i < word.getLength(); i++) {
            int r = word.getRow() + (word.getDirection() == Direction.DOWN ? i : 0);
            int c = word.getCol() + (word.getDirection() == Direction.ACROSS ? i : 0);
            revealCell(r, c);
        }
        lockWordIfComplete(word);
    }

    /**
     * Print the puzzle grid for debugging.
     */
    public void printGrid() {
        System.out.println("Crossword Puzzle (" + gridWidth + "x" + gridHeight + ")");
        for (int r = 0; r < gridHeight; r++) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < gridWidth; c++) {
                CrosswordCell cell = grid[r][c];
                if (cell.isBlocked()) {
                    sb.append("# ");
                } else if (cell.isEmpty()) {
                    sb.append("_ ");
                } else {
                    sb.append(cell.getUserEntry()).append(" ");
                }
            }
            System.out.println(sb);
        }
    }

    /**
     * Print the solution grid for debugging.
     */
    public void printSolution() {
        System.out.println("Solution:");
        for (int r = 0; r < gridHeight; r++) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < gridWidth; c++) {
                CrosswordCell cell = grid[r][c];
                if (cell.isBlocked()) {
                    sb.append("# ");
                } else {
                    sb.append(cell.getSolution()).append(" ");
                }
            }
            System.out.println(sb);
        }
    }
}
