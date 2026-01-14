package com.heronix.edu.games.crossword.model;

/**
 * Direction of word placement in the crossword grid.
 */
public enum Direction {
    ACROSS,
    DOWN;

    /**
     * Get the opposite direction.
     */
    public Direction opposite() {
        return this == ACROSS ? DOWN : ACROSS;
    }

    /**
     * Get the row offset for this direction.
     */
    public int getRowOffset() {
        return this == DOWN ? 1 : 0;
    }

    /**
     * Get the column offset for this direction.
     */
    public int getColOffset() {
        return this == ACROSS ? 1 : 0;
    }
}
