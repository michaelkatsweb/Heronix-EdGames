package com.heronix.edu.games.crossword.generator;

import com.heronix.edu.common.game.DifficultyLevel;
import com.heronix.edu.games.crossword.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates crossword puzzles from a word list.
 * Uses a greedy algorithm to maximize word intersections.
 */
public class CrosswordGenerator {

    // Difficulty settings: gridSize, targetWords, minLength, maxLength
    private static final Map<DifficultyLevel, int[]> DIFFICULTY_SETTINGS = Map.of(
        DifficultyLevel.EASY, new int[]{10, 8, 3, 6},
        DifficultyLevel.MEDIUM, new int[]{13, 12, 4, 8},
        DifficultyLevel.HARD, new int[]{16, 18, 5, 12},
        DifficultyLevel.EXPERT, new int[]{20, 25, 6, 15}
    );

    // Working grid size (larger to allow flexibility)
    private static final int WORKING_GRID_SIZE = 30;

    private final Random random;

    public CrosswordGenerator() {
        this.random = new Random();
    }

    public CrosswordGenerator(long seed) {
        this.random = new Random(seed);
    }

    /**
     * Generate a crossword puzzle from a word list at the specified difficulty.
     */
    public CrosswordPuzzle generate(WordList wordList, DifficultyLevel difficulty) {
        int[] settings = DIFFICULTY_SETTINGS.getOrDefault(difficulty, DIFFICULTY_SETTINGS.get(DifficultyLevel.MEDIUM));
        int targetGridSize = settings[0];
        int targetWords = settings[1];
        int minLength = settings[2];
        int maxLength = settings[3];

        // Filter and sort words by length (longest first)
        List<WordEntry> availableWords = wordList.getEntries().stream()
                .filter(w -> w.getWord().length() >= minLength && w.getWord().length() <= maxLength)
                .sorted((a, b) -> b.getWord().length() - a.getWord().length())
                .collect(Collectors.toList());

        if (availableWords.isEmpty()) {
            throw new IllegalArgumentException("No words available for the specified difficulty");
        }

        // Shuffle to add variety while keeping general length order
        shuffleWithLengthBias(availableWords);

        // Create placer with working grid
        GridPlacer placer = new GridPlacer(WORKING_GRID_SIZE, WORKING_GRID_SIZE);
        List<PlacedWord> placedWords = new ArrayList<>();

        // Place words
        int attempts = 0;
        int maxAttempts = availableWords.size() * 3;

        for (WordEntry entry : availableWords) {
            if (placedWords.size() >= targetWords) {
                break;
            }
            if (attempts++ > maxAttempts) {
                break;
            }

            String word = entry.getWord().toUpperCase();
            boolean isFirst = placedWords.isEmpty();

            PlacementResult result = placer.findBestPlacement(word, isFirst);

            if (result.isValid()) {
                placer.placeWord(word, result.getRow(), result.getCol(), result.getDirection());
                placedWords.add(new PlacedWord(entry, result.getRow(), result.getCol(), result.getDirection()));
            }
        }

        if (placedWords.isEmpty()) {
            throw new IllegalStateException("Could not place any words");
        }

        // Compact the grid and create the puzzle
        return createPuzzle(placer, placedWords, targetGridSize, wordList.getSubject(), difficulty);
    }

    /**
     * Shuffle words while keeping longer words earlier.
     */
    private void shuffleWithLengthBias(List<WordEntry> words) {
        // Group by length and shuffle within groups
        Map<Integer, List<WordEntry>> byLength = words.stream()
                .collect(Collectors.groupingBy(w -> w.getWord().length()));

        words.clear();
        List<Integer> lengths = new ArrayList<>(byLength.keySet());
        Collections.sort(lengths, Collections.reverseOrder());

        for (int length : lengths) {
            List<WordEntry> group = byLength.get(length);
            Collections.shuffle(group, random);
            words.addAll(group);
        }
    }

    /**
     * Create the final puzzle from the placer grid.
     */
    private CrosswordPuzzle createPuzzle(GridPlacer placer, List<PlacedWord> placedWords,
                                          int maxGridSize, String subject, DifficultyLevel difficulty) {
        // Get bounding box
        int[] bbox = placer.getBoundingBox();
        int minRow = bbox[0], minCol = bbox[1], maxRow = bbox[2], maxCol = bbox[3];

        // Calculate actual grid size with 1-cell padding
        int gridHeight = Math.min(maxRow - minRow + 3, maxGridSize);
        int gridWidth = Math.min(maxCol - minCol + 3, maxGridSize);

        // Ensure minimum size
        gridWidth = Math.max(gridWidth, 8);
        gridHeight = Math.max(gridHeight, 8);

        // Create puzzle
        CrosswordPuzzle puzzle = new CrosswordPuzzle(gridWidth, gridHeight);
        puzzle.setSubject(subject);
        puzzle.setDifficulty(difficulty);

        // Offset to center content
        int rowOffset = -minRow + 1;
        int colOffset = -minCol + 1;

        // Transfer placed words
        List<WordStartPosition> startPositions = new ArrayList<>();

        for (PlacedWord pw : placedWords) {
            int newRow = pw.row + rowOffset;
            int newCol = pw.col + colOffset;

            // Clamp to grid bounds
            if (newRow < 0 || newCol < 0 ||
                newRow + (pw.direction == Direction.DOWN ? pw.entry.getWord().length() : 0) > gridHeight ||
                newCol + (pw.direction == Direction.ACROSS ? pw.entry.getWord().length() : 0) > gridWidth) {
                continue;
            }

            CrosswordWord word = new CrosswordWord(
                    pw.entry.getWord().toUpperCase(),
                    pw.entry.getClue(),
                    newRow,
                    newCol,
                    pw.direction
            );

            puzzle.addWord(word);
            startPositions.add(new WordStartPosition(newRow, newCol, pw.direction, word));
        }

        // Assign clue numbers
        assignClueNumbers(puzzle, startPositions);

        // Build clue lists
        puzzle.buildClueLists();

        return puzzle;
    }

    /**
     * Assign clue numbers based on position (top-to-bottom, left-to-right).
     */
    private void assignClueNumbers(CrosswordPuzzle puzzle, List<WordStartPosition> startPositions) {
        // Sort by position
        startPositions.sort((a, b) -> {
            if (a.row != b.row) return a.row - b.row;
            return a.col - b.col;
        });

        // Assign numbers
        Map<String, Integer> positionNumbers = new HashMap<>();
        int nextNumber = 1;

        for (WordStartPosition pos : startPositions) {
            String key = pos.row + "," + pos.col;
            int number;

            if (positionNumbers.containsKey(key)) {
                number = positionNumbers.get(key);
            } else {
                number = nextNumber++;
                positionNumbers.put(key, number);
            }

            pos.word.setClueNumber(number);

            // Update cell clue number
            CrosswordCell cell = puzzle.getCell(pos.row, pos.col);
            if (cell != null) {
                if (pos.direction == Direction.ACROSS) {
                    cell.setAcrossClueNum(number);
                } else {
                    cell.setDownClueNum(number);
                }
            }
        }
    }

    // Helper classes

    private static class PlacedWord {
        final WordEntry entry;
        final int row;
        final int col;
        final Direction direction;

        PlacedWord(WordEntry entry, int row, int col, Direction direction) {
            this.entry = entry;
            this.row = row;
            this.col = col;
            this.direction = direction;
        }
    }

    private static class WordStartPosition {
        final int row;
        final int col;
        final Direction direction;
        final CrosswordWord word;

        WordStartPosition(int row, int col, Direction direction, CrosswordWord word) {
            this.row = row;
            this.col = col;
            this.direction = direction;
            this.word = word;
        }
    }
}
