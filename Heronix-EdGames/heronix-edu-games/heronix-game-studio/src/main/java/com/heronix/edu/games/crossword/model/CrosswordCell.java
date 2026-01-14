package com.heronix.edu.games.crossword.model;

/**
 * Represents a single cell in the crossword grid.
 * Tracks the solution letter, user input, and cell state.
 */
public class CrosswordCell {
    private int row;
    private int col;
    private char solution;          // The correct letter (uppercase)
    private char userEntry;         // What the user typed (0 if empty)
    private boolean blocked;        // Black/blocked cell
    private boolean locked;         // Correct answer locked in
    private boolean revealed;       // Revealed via hint
    private int acrossClueNum;      // Clue number if this is start of across word (0 otherwise)
    private int downClueNum;        // Clue number if this is start of down word (0 otherwise)

    public CrosswordCell(int row, int col) {
        this.row = row;
        this.col = col;
        this.blocked = true;  // Default to blocked until a word is placed
        this.solution = '\0';
        this.userEntry = '\0';
    }

    public CrosswordCell(int row, int col, char solution) {
        this.row = row;
        this.col = col;
        this.solution = Character.toUpperCase(solution);
        this.blocked = false;
        this.userEntry = '\0';
    }

    // Getters and setters

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public char getSolution() {
        return solution;
    }

    public void setSolution(char solution) {
        this.solution = Character.toUpperCase(solution);
        this.blocked = false;
    }

    public char getUserEntry() {
        return userEntry;
    }

    public void setUserEntry(char userEntry) {
        if (!locked && !blocked) {
            this.userEntry = Character.toUpperCase(userEntry);
        }
    }

    public void clearUserEntry() {
        if (!locked && !blocked) {
            this.userEntry = '\0';
        }
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    public int getAcrossClueNum() {
        return acrossClueNum;
    }

    public void setAcrossClueNum(int acrossClueNum) {
        this.acrossClueNum = acrossClueNum;
    }

    public int getDownClueNum() {
        return downClueNum;
    }

    public void setDownClueNum(int downClueNum) {
        this.downClueNum = downClueNum;
    }

    /**
     * Check if this cell has a clue number (start of a word).
     */
    public boolean hasClueNumber() {
        return acrossClueNum > 0 || downClueNum > 0;
    }

    /**
     * Get the display clue number (first non-zero).
     */
    public int getDisplayClueNumber() {
        return acrossClueNum > 0 ? acrossClueNum : downClueNum;
    }

    /**
     * Check if the cell is empty (no user entry).
     */
    public boolean isEmpty() {
        return userEntry == '\0';
    }

    /**
     * Check if the cell has content (letter placed).
     */
    public boolean hasContent() {
        return !blocked && solution != '\0';
    }

    /**
     * Check if the user's entry is correct.
     */
    public boolean isCorrect() {
        return !blocked && userEntry == solution;
    }

    /**
     * Check if the user's entry is incorrect (wrong letter, not empty).
     */
    public boolean isIncorrect() {
        return !blocked && userEntry != '\0' && userEntry != solution;
    }

    /**
     * Check if this cell is filled (either correct or incorrect).
     */
    public boolean isFilled() {
        return !blocked && userEntry != '\0';
    }

    /**
     * Reveal the solution for this cell.
     */
    public void reveal() {
        if (!blocked && !locked) {
            this.userEntry = this.solution;
            this.revealed = true;
            this.locked = true;
        }
    }

    /**
     * Lock the cell if the answer is correct.
     */
    public void lockIfCorrect() {
        if (isCorrect()) {
            this.locked = true;
        }
    }

    @Override
    public String toString() {
        if (blocked) return "#";
        if (isEmpty()) return "_";
        return String.valueOf(userEntry);
    }
}
