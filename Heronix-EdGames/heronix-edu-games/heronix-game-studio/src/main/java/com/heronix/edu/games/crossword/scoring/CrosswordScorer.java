package com.heronix.edu.games.crossword.scoring;

import com.heronix.edu.common.game.DifficultyLevel;
import com.heronix.edu.games.crossword.model.CrosswordPuzzle;

/**
 * Calculates scores for crossword puzzle gameplay.
 *
 * Scoring formula:
 * - Base: 10 points per correct cell
 * - Word bonus: +25 points per completed word
 * - No hints bonus: +100 points
 * - No errors bonus: +50 points
 * - Time bonus: Based on par time for difficulty
 * - Penalties: -5 per hint used, -2 per error
 * - Difficulty multiplier: EASY 1.0, MEDIUM 1.25, HARD 1.5, EXPERT 2.0
 */
public class CrosswordScorer {

    // Scoring constants
    private static final int POINTS_PER_CELL = 10;
    private static final int POINTS_PER_WORD = 25;
    private static final int NO_HINTS_BONUS = 100;
    private static final int NO_ERRORS_BONUS = 50;
    private static final int HINT_PENALTY = 5;
    private static final int ERROR_PENALTY = 2;

    // Par times in seconds for each difficulty
    private static final int PAR_TIME_EASY = 80;
    private static final int PAR_TIME_MEDIUM = 96;
    private static final int PAR_TIME_HARD = 108;
    private static final int PAR_TIME_EXPERT = 125;

    // Time bonus constants
    private static final int MAX_TIME_BONUS = 200;

    private final CrosswordPuzzle puzzle;
    private final DifficultyLevel difficulty;
    private final double difficultyMultiplier;
    private final int parTime;

    public CrosswordScorer(CrosswordPuzzle puzzle, DifficultyLevel difficulty) {
        this.puzzle = puzzle;
        this.difficulty = difficulty;
        this.difficultyMultiplier = getDifficultyMultiplier(difficulty);
        this.parTime = getParTime(difficulty);
    }

    private double getDifficultyMultiplier(DifficultyLevel level) {
        switch (level) {
            case EASY: return 1.0;
            case MEDIUM: return 1.25;
            case HARD: return 1.5;
            case EXPERT: return 2.0;
            default: return 1.0;
        }
    }

    private int getParTime(DifficultyLevel level) {
        switch (level) {
            case EASY: return PAR_TIME_EASY;
            case MEDIUM: return PAR_TIME_MEDIUM;
            case HARD: return PAR_TIME_HARD;
            case EXPERT: return PAR_TIME_EXPERT;
            default: return PAR_TIME_MEDIUM;
        }
    }

    /**
     * Calculate current score during gameplay.
     */
    public int calculateCurrentScore(int hintsUsed, int errorsCommitted, int elapsedSeconds) {
        int baseScore = 0;

        // Points for solved cells
        int solvedCells = puzzle.getSolvedCells();
        baseScore += solvedCells * POINTS_PER_CELL;

        // Points for completed words
        int completedWords = puzzle.getCompletedWordCount();
        baseScore += completedWords * POINTS_PER_WORD;

        // Penalties
        int penalties = (hintsUsed * HINT_PENALTY) + (errorsCommitted * ERROR_PENALTY);
        baseScore -= penalties;

        // Apply difficulty multiplier
        int finalScore = (int) (baseScore * difficultyMultiplier);

        return Math.max(0, finalScore);
    }

    /**
     * Calculate final score when puzzle is completed.
     */
    public int calculateFinalScore(int hintsUsed, int errorsCommitted, int elapsedSeconds, boolean completed) {
        if (!completed) {
            return calculateCurrentScore(hintsUsed, errorsCommitted, elapsedSeconds);
        }

        int baseScore = 0;

        // Full points for all cells
        int totalCells = puzzle.getTotalCells();
        baseScore += totalCells * POINTS_PER_CELL;

        // Points for all words
        int totalWords = puzzle.getPlacedWords().size();
        baseScore += totalWords * POINTS_PER_WORD;

        // Bonuses
        if (hintsUsed == 0) {
            baseScore += NO_HINTS_BONUS;
        }
        if (errorsCommitted == 0) {
            baseScore += NO_ERRORS_BONUS;
        }

        // Time bonus (if completed under par time)
        int timeBonus = calculateTimeBonus(elapsedSeconds);
        baseScore += timeBonus;

        // Penalties
        int penalties = (hintsUsed * HINT_PENALTY) + (errorsCommitted * ERROR_PENALTY);
        baseScore -= penalties;

        // Apply difficulty multiplier
        int finalScore = (int) (baseScore * difficultyMultiplier);

        return Math.max(0, finalScore);
    }

    /**
     * Calculate time bonus based on completion time.
     */
    private int calculateTimeBonus(int elapsedSeconds) {
        if (elapsedSeconds >= parTime * 2) {
            return 0; // No bonus if took more than double par time
        }

        if (elapsedSeconds <= parTime) {
            // Under par: full bonus scaled by how much under
            double ratio = (double) (parTime - elapsedSeconds) / parTime;
            return (int) (MAX_TIME_BONUS * (1 + ratio * 0.5));
        } else {
            // Over par: reduced bonus
            double ratio = (double) (parTime * 2 - elapsedSeconds) / parTime;
            return (int) (MAX_TIME_BONUS * ratio * 0.5);
        }
    }

    /**
     * Get breakdown of score components.
     */
    public ScoreBreakdown getScoreBreakdown(int hintsUsed, int errorsCommitted, int elapsedSeconds, boolean completed) {
        ScoreBreakdown breakdown = new ScoreBreakdown();

        int solvedCells = completed ? puzzle.getTotalCells() : puzzle.getSolvedCells();
        int completedWords = completed ? puzzle.getPlacedWords().size() : puzzle.getCompletedWordCount();

        breakdown.cellPoints = solvedCells * POINTS_PER_CELL;
        breakdown.wordPoints = completedWords * POINTS_PER_WORD;
        breakdown.noHintsBonus = (completed && hintsUsed == 0) ? NO_HINTS_BONUS : 0;
        breakdown.noErrorsBonus = (completed && errorsCommitted == 0) ? NO_ERRORS_BONUS : 0;
        breakdown.timeBonus = completed ? calculateTimeBonus(elapsedSeconds) : 0;
        breakdown.hintPenalty = hintsUsed * HINT_PENALTY;
        breakdown.errorPenalty = errorsCommitted * ERROR_PENALTY;
        breakdown.difficultyMultiplier = difficultyMultiplier;

        int subtotal = breakdown.cellPoints + breakdown.wordPoints +
                       breakdown.noHintsBonus + breakdown.noErrorsBonus + breakdown.timeBonus -
                       breakdown.hintPenalty - breakdown.errorPenalty;

        breakdown.totalScore = (int) (subtotal * difficultyMultiplier);

        return breakdown;
    }

    /**
     * Score breakdown details.
     */
    public static class ScoreBreakdown {
        public int cellPoints;
        public int wordPoints;
        public int noHintsBonus;
        public int noErrorsBonus;
        public int timeBonus;
        public int hintPenalty;
        public int errorPenalty;
        public double difficultyMultiplier;
        public int totalScore;

        @Override
        public String toString() {
            return String.format(
                "Score Breakdown:\n" +
                "  Cell Points: %d\n" +
                "  Word Points: %d\n" +
                "  No Hints Bonus: %d\n" +
                "  No Errors Bonus: %d\n" +
                "  Time Bonus: %d\n" +
                "  Hint Penalty: -%d\n" +
                "  Error Penalty: -%d\n" +
                "  Difficulty Multiplier: x%.2f\n" +
                "  TOTAL: %d",
                cellPoints, wordPoints, noHintsBonus, noErrorsBonus, timeBonus,
                hintPenalty, errorPenalty, difficultyMultiplier, totalScore
            );
        }
    }

    // Getters

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public double getDifficultyMultiplier() {
        return difficultyMultiplier;
    }

    public int getParTime() {
        return parTime;
    }
}
