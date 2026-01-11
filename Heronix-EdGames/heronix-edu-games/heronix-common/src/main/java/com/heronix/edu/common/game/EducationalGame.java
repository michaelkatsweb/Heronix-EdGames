package com.heronix.edu.common.game;

/**
 * Core interface that all educational games must implement.
 * This defines the contract for game lifecycle and integration with the platform.
 */
public interface EducationalGame {
    
    /**
     * Get unique game identifier
     * @return game ID (e.g., "math-sprint", "word-builder")
     */
    String getGameId();
    
    /**
     * Get human-readable game name
     * @return game name (e.g., "Math Sprint", "Word Builder")
     */
    String getName();
    
    /**
     * Get game description
     * @return brief description of the game
     */
    String getDescription();
    
    /**
     * Get educational subject
     * @return subject area (e.g., "Mathematics", "Language Arts", "Science")
     */
    GameSubject getSubject();
    
    /**
     * Get target grade levels
     * @return array of grade levels this game is suitable for
     */
    GradeLevel[] getTargetGrades();
    
    /**
     * Get game version
     * @return version string (e.g., "1.0.0")
     */
    String getVersion();
    
    /**
     * Initialize the game with context
     * @param context game context containing student info, settings, etc.
     */
    void initialize(GameContext context);
    
    /**
     * Start the game
     */
    void start();
    
    /**
     * Pause the game
     */
    void pause();
    
    /**
     * Resume a paused game
     */
    void resume();
    
    /**
     * Stop the game
     */
    void stop();
    
    /**
     * Get current game result/score
     * @return current game result
     */
    GameResult getResult();
    
    /**
     * Get game state for saving
     * @return serializable game state
     */
    GameState getState();
    
    /**
     * Restore game from saved state
     * @param state previously saved game state
     */
    void restoreState(GameState state);
    
    /**
     * Check if game supports difficulty levels
     * @return true if game has multiple difficulty levels
     */
    boolean supportsDifficulty();
    
    /**
     * Get available difficulty levels
     * @return array of difficulty levels, or null if not supported
     */
    DifficultyLevel[] getDifficultyLevels();
    
    /**
     * Set difficulty level
     * @param level difficulty level to set
     */
    void setDifficulty(DifficultyLevel level);
}
