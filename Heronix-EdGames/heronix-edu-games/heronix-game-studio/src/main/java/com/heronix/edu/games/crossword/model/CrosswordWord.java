package com.heronix.edu.games.crossword.model;

/**
 * Represents a word placed in the crossword puzzle.
 * Stores the word, its clue, position, and direction.
 */
public class CrosswordWord {
    private String word;            // The word (uppercase)
    private String clue;            // The clue text
    private int row;                // Starting row
    private int col;                // Starting column
    private Direction direction;    // ACROSS or DOWN
    private int clueNumber;         // Assigned clue number
    private String category;        // Optional category

    public CrosswordWord() {}

    public CrosswordWord(String word, String clue, int row, int col, Direction direction) {
        this.word = word.toUpperCase();
        this.clue = clue;
        this.row = row;
        this.col = col;
        this.direction = direction;
    }

    public CrosswordWord(WordEntry entry, int row, int col, Direction direction) {
        this.word = entry.getWord();
        this.clue = entry.getClue();
        this.category = entry.getCategory();
        this.row = row;
        this.col = col;
        this.direction = direction;
    }

    // Getters and setters

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word != null ? word.toUpperCase() : null;
    }

    public String getClue() {
        return clue;
    }

    public void setClue(String clue) {
        this.clue = clue;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getClueNumber() {
        return clueNumber;
    }

    public void setClueNumber(int clueNumber) {
        this.clueNumber = clueNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Get the length of the word.
     */
    public int getLength() {
        return word != null ? word.length() : 0;
    }

    /**
     * Get the ending row of the word.
     */
    public int getEndRow() {
        return direction == Direction.DOWN ? row + getLength() - 1 : row;
    }

    /**
     * Get the ending column of the word.
     */
    public int getEndCol() {
        return direction == Direction.ACROSS ? col + getLength() - 1 : col;
    }

    /**
     * Check if a position is within this word.
     */
    public boolean containsPosition(int r, int c) {
        if (direction == Direction.ACROSS) {
            return r == row && c >= col && c <= getEndCol();
        } else {
            return c == col && r >= row && r <= getEndRow();
        }
    }

    /**
     * Get the index of a letter at the given position (0-based).
     * Returns -1 if position is not in this word.
     */
    public int getLetterIndex(int r, int c) {
        if (!containsPosition(r, c)) return -1;
        return direction == Direction.ACROSS ? c - col : r - row;
    }

    /**
     * Get the letter at the given position.
     * Returns '\0' if position is not in this word.
     */
    public char getLetterAt(int r, int c) {
        int idx = getLetterIndex(r, c);
        return idx >= 0 && idx < word.length() ? word.charAt(idx) : '\0';
    }

    /**
     * Check if this word intersects with another word.
     */
    public boolean intersects(CrosswordWord other) {
        if (this.direction == other.direction) {
            return false; // Parallel words don't intersect
        }

        CrosswordWord across = direction == Direction.ACROSS ? this : other;
        CrosswordWord down = direction == Direction.DOWN ? this : other;

        // Check if the down word's column is within the across word's span
        if (down.col < across.col || down.col > across.getEndCol()) {
            return false;
        }

        // Check if the across word's row is within the down word's span
        if (across.row < down.row || across.row > down.getEndRow()) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "CrosswordWord{" + clueNumber + " " + direction +
               ": '" + word + "' at (" + row + "," + col + ")}";
    }
}
