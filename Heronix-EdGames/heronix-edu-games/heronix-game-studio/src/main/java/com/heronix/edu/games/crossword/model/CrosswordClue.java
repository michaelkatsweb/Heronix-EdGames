package com.heronix.edu.games.crossword.model;

/**
 * Represents a clue displayed to the player.
 * Contains the clue number, text, direction, and solved status.
 */
public class CrosswordClue {
    private int number;             // Clue number (e.g., 1, 2, 3...)
    private String text;            // The clue text
    private Direction direction;    // ACROSS or DOWN
    private CrosswordWord wordRef;  // Reference to the placed word
    private boolean solved;         // Whether the word has been solved

    public CrosswordClue() {}

    public CrosswordClue(int number, String text, Direction direction, CrosswordWord wordRef) {
        this.number = number;
        this.text = text;
        this.direction = direction;
        this.wordRef = wordRef;
        this.solved = false;
    }

    public static CrosswordClue fromWord(CrosswordWord word) {
        return new CrosswordClue(
            word.getClueNumber(),
            word.getClue(),
            word.getDirection(),
            word
        );
    }

    // Getters and setters

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public CrosswordWord getWordRef() {
        return wordRef;
    }

    public void setWordRef(CrosswordWord wordRef) {
        this.wordRef = wordRef;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    /**
     * Get the word length for display (e.g., "(5 letters)").
     */
    public int getWordLength() {
        return wordRef != null ? wordRef.getLength() : 0;
    }

    /**
     * Get formatted clue for display.
     * Format: "1. The clue text (5)"
     */
    public String getFormattedClue() {
        return number + ". " + text + " (" + getWordLength() + ")";
    }

    /**
     * Get short formatted clue without length.
     * Format: "1. The clue text"
     */
    public String getShortFormattedClue() {
        return number + ". " + text;
    }

    @Override
    public String toString() {
        return getFormattedClue();
    }
}
