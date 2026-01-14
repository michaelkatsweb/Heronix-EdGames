package com.heronix.edu.games.crossword.generator;

import com.heronix.edu.games.crossword.model.Direction;

/**
 * Result of attempting to place a word on the grid.
 * Contains position, direction, and scoring information.
 */
public class PlacementResult {
    private final int row;
    private final int col;
    private final Direction direction;
    private final int score;
    private final int intersections;
    private final boolean valid;

    private PlacementResult(int row, int col, Direction direction, int score, int intersections, boolean valid) {
        this.row = row;
        this.col = col;
        this.direction = direction;
        this.score = score;
        this.intersections = intersections;
        this.valid = valid;
    }

    /**
     * Create an invalid placement result.
     */
    public static PlacementResult invalid() {
        return new PlacementResult(-1, -1, null, Integer.MIN_VALUE, 0, false);
    }

    /**
     * Create a valid placement result.
     */
    public static PlacementResult of(int row, int col, Direction direction, int score, int intersections) {
        return new PlacementResult(row, col, direction, score, intersections, true);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getScore() {
        return score;
    }

    public int getIntersections() {
        return intersections;
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public String toString() {
        if (!valid) {
            return "PlacementResult[invalid]";
        }
        return String.format("PlacementResult[row=%d, col=%d, dir=%s, score=%d, intersections=%d]",
                row, col, direction, score, intersections);
    }
}
