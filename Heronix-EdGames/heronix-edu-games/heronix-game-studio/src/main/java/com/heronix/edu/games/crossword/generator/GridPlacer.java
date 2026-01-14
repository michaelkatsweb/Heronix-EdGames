package com.heronix.edu.games.crossword.generator;

import com.heronix.edu.games.crossword.model.Direction;

/**
 * Handles word placement logic on a crossword grid.
 * Validates positions and calculates placement scores.
 */
public class GridPlacer {
    private final char[][] grid;
    private final int width;
    private final int height;

    // Empty cell marker
    private static final char EMPTY = '\0';

    public GridPlacer(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new char[height][width];
    }

    /**
     * Get the current grid state.
     */
    public char[][] getGrid() {
        return grid;
    }

    /**
     * Check if a position is within grid bounds.
     */
    public boolean isInBounds(int row, int col) {
        return row >= 0 && row < height && col >= 0 && col < width;
    }

    /**
     * Check if a cell is empty.
     */
    public boolean isEmpty(int row, int col) {
        return isInBounds(row, col) && grid[row][col] == EMPTY;
    }

    /**
     * Get the character at a position.
     */
    public char getChar(int row, int col) {
        if (!isInBounds(row, col)) {
            return EMPTY;
        }
        return grid[row][col];
    }

    /**
     * Try to find the best placement for a word.
     * Returns the best PlacementResult or an invalid result if no placement found.
     */
    public PlacementResult findBestPlacement(String word, boolean isFirstWord) {
        if (isFirstWord) {
            return placeFirstWord(word);
        }

        PlacementResult bestResult = PlacementResult.invalid();

        // Try to place at each existing letter position
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (grid[r][c] != EMPTY) {
                    // Try both directions at intersections
                    for (Direction dir : Direction.values()) {
                        PlacementResult result = tryPlacement(word, r, c, dir);
                        if (result.isValid() && result.getScore() > bestResult.getScore()) {
                            bestResult = result;
                        }
                    }
                }
            }
        }

        return bestResult;
    }

    /**
     * Place the first word in the center of the grid.
     */
    private PlacementResult placeFirstWord(String word) {
        int startRow = height / 2;
        int startCol = (width - word.length()) / 2;

        if (startCol < 0) {
            return PlacementResult.invalid();
        }

        return PlacementResult.of(startRow, startCol, Direction.ACROSS, 100, 0);
    }

    /**
     * Try to place a word at a position intersecting with an existing letter.
     */
    private PlacementResult tryPlacement(String word, int intersectRow, int intersectCol, Direction direction) {
        char intersectChar = grid[intersectRow][intersectCol];

        // Find where this character appears in the word
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == intersectChar) {
                // Calculate start position if we place the word here
                int startRow = intersectRow - (direction == Direction.DOWN ? i : 0);
                int startCol = intersectCol - (direction == Direction.ACROSS ? i : 0);

                PlacementResult result = validatePlacement(word, startRow, startCol, direction);
                if (result.isValid()) {
                    return result;
                }
            }
        }

        return PlacementResult.invalid();
    }

    /**
     * Validate a word placement and calculate its score.
     */
    public PlacementResult validatePlacement(String word, int startRow, int startCol, Direction direction) {
        int rowOffset = direction.getRowOffset();
        int colOffset = direction.getColOffset();

        // Check bounds
        int endRow = startRow + (direction == Direction.DOWN ? word.length() - 1 : 0);
        int endCol = startCol + (direction == Direction.ACROSS ? word.length() - 1 : 0);

        if (!isInBounds(startRow, startCol) || !isInBounds(endRow, endCol)) {
            return PlacementResult.invalid();
        }

        // Check cell before word (should be empty or out of bounds)
        int beforeRow = startRow - rowOffset;
        int beforeCol = startCol - colOffset;
        if (isInBounds(beforeRow, beforeCol) && grid[beforeRow][beforeCol] != EMPTY) {
            return PlacementResult.invalid();
        }

        // Check cell after word (should be empty or out of bounds)
        int afterRow = startRow + (rowOffset * word.length());
        int afterCol = startCol + (colOffset * word.length());
        if (isInBounds(afterRow, afterCol) && grid[afterRow][afterCol] != EMPTY) {
            return PlacementResult.invalid();
        }

        int intersections = 0;
        int score = 0;

        // Check each letter position
        for (int i = 0; i < word.length(); i++) {
            int row = startRow + (rowOffset * i);
            int col = startCol + (colOffset * i);
            char wordChar = word.charAt(i);
            char gridChar = grid[row][col];

            if (gridChar != EMPTY) {
                // Cell is occupied
                if (gridChar != wordChar) {
                    // Conflict - different letter
                    return PlacementResult.invalid();
                }
                // Valid intersection
                intersections++;
                score += 10; // Bonus for intersection
            } else {
                // Cell is empty - check adjacent cells perpendicular to direction
                if (!checkAdjacentCells(row, col, direction, wordChar)) {
                    return PlacementResult.invalid();
                }
            }
        }

        // Must have at least one intersection (except first word)
        // Calculate final score
        score += word.length() * 5; // Base score for word length
        score += intersections * 15; // Bonus for intersections

        return PlacementResult.of(startRow, startCol, direction, score, intersections);
    }

    /**
     * Check that adjacent cells perpendicular to placement don't conflict.
     */
    private boolean checkAdjacentCells(int row, int col, Direction direction, char placingChar) {
        if (direction == Direction.ACROSS) {
            // Check above and below
            char above = getChar(row - 1, col);
            char below = getChar(row + 1, col);

            // If there's an adjacent filled cell, it would create an invalid word segment
            if (above != EMPTY || below != EMPTY) {
                return false;
            }
        } else {
            // Check left and right
            char left = getChar(row, col - 1);
            char right = getChar(row, col + 1);

            if (left != EMPTY || right != EMPTY) {
                return false;
            }
        }
        return true;
    }

    /**
     * Place a word on the grid.
     */
    public void placeWord(String word, int startRow, int startCol, Direction direction) {
        int rowOffset = direction.getRowOffset();
        int colOffset = direction.getColOffset();

        for (int i = 0; i < word.length(); i++) {
            int row = startRow + (rowOffset * i);
            int col = startCol + (colOffset * i);
            grid[row][col] = word.charAt(i);
        }
    }

    /**
     * Get the bounding box of placed words.
     * Returns [minRow, minCol, maxRow, maxCol].
     */
    public int[] getBoundingBox() {
        int minRow = height, minCol = width, maxRow = -1, maxCol = -1;

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (grid[r][c] != EMPTY) {
                    minRow = Math.min(minRow, r);
                    minCol = Math.min(minCol, c);
                    maxRow = Math.max(maxRow, r);
                    maxCol = Math.max(maxCol, c);
                }
            }
        }

        if (maxRow < 0) {
            return new int[]{0, 0, 0, 0};
        }

        return new int[]{minRow, minCol, maxRow, maxCol};
    }

    /**
     * Print the grid for debugging.
     */
    public void printGrid() {
        System.out.println("Grid (" + width + "x" + height + "):");
        for (int r = 0; r < height; r++) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < width; c++) {
                char ch = grid[r][c];
                sb.append(ch == EMPTY ? "." : ch).append(" ");
            }
            System.out.println(sb);
        }
    }
}
