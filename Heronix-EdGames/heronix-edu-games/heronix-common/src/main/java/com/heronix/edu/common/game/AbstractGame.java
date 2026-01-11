package com.heronix.edu.common.game;

/**
 * Abstract base class providing common functionality for educational games
 */
public abstract class AbstractGame implements EducationalGame {
    
    protected GameContext context;
    protected GameResult result;
    protected GameState currentState;
    protected boolean isRunning;
    protected boolean isPaused;
    protected long startTimeMillis;
    protected long pauseTimeMillis;
    protected long totalPausedMillis;
    
    public AbstractGame() {
        this.isRunning = false;
        this.isPaused = false;
        this.totalPausedMillis = 0;
    }
    
    @Override
    public void initialize(GameContext context) {
        this.context = context;
        this.result = new GameResult(getGameId());
        this.result.setDifficultyLevel(context.getDifficultyLevel());
        onInitialize();
    }
    
    @Override
    public void start() {
        if (!isRunning) {
            this.isRunning = true;
            this.isPaused = false;
            this.startTimeMillis = System.currentTimeMillis();
            this.result.setStartTime(java.time.LocalDateTime.now());
            onStart();
        }
    }
    
    @Override
    public void pause() {
        if (isRunning && !isPaused) {
            this.isPaused = true;
            this.pauseTimeMillis = System.currentTimeMillis();
            onPause();
        }
    }
    
    @Override
    public void resume() {
        if (isRunning && isPaused) {
            this.isPaused = false;
            this.totalPausedMillis += System.currentTimeMillis() - pauseTimeMillis;
            onResume();
        }
    }
    
    @Override
    public void stop() {
        if (isRunning) {
            this.isRunning = false;
            this.isPaused = false;
            updateElapsedTime();
            this.result.setCompleted(true);
            onStop();
        }
    }
    
    @Override
    public GameResult getResult() {
        if (isRunning && !isPaused) {
            updateElapsedTime();
        }
        return result;
    }
    
    @Override
    public GameState getState() {
        GameState state = new GameState(getGameId());
        state.setCurrentScore(result.getScore());
        state.setTimeElapsed(result.getTimeElapsedSeconds());
        state.setDifficultyLevel(result.getDifficultyLevel());
        saveStateData(state);
        return state;
    }
    
    @Override
    public void restoreState(GameState state) {
        if (!state.getGameId().equals(getGameId())) {
            throw new IllegalArgumentException("Invalid game state for " + getGameId());
        }
        this.currentState = state;
        this.result.setScore(state.getCurrentScore());
        this.result.setTimeElapsedSeconds(state.getTimeElapsed());
        this.result.setDifficultyLevel(state.getDifficultyLevel());
        loadStateData(state);
    }
    
    @Override
    public boolean supportsDifficulty() {
        return getDifficultyLevels() != null && getDifficultyLevels().length > 0;
    }
    
    @Override
    public DifficultyLevel[] getDifficultyLevels() {
        // Default: support all levels
        return DifficultyLevel.values();
    }
    
    @Override
    public void setDifficulty(DifficultyLevel level) {
        if (context != null) {
            context.setDifficultyLevel(level);
        }
        if (result != null) {
            result.setDifficultyLevel(level);
        }
    }
    
    /**
     * Update elapsed time in result
     */
    protected void updateElapsedTime() {
        if (startTimeMillis > 0) {
            long elapsed = System.currentTimeMillis() - startTimeMillis - totalPausedMillis;
            result.setTimeElapsedSeconds((int) (elapsed / 1000));
        }
    }
    
    /**
     * Check if game is currently running
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Check if game is paused
     */
    public boolean isPaused() {
        return isPaused;
    }
    
    /**
     * Get game context
     */
    public GameContext getContext() {
        return context;
    }
    
    // Template methods for subclasses to implement
    
    /**
     * Called when game is initialized
     */
    protected abstract void onInitialize();
    
    /**
     * Called when game starts
     */
    protected abstract void onStart();
    
    /**
     * Called when game is paused
     */
    protected abstract void onPause();
    
    /**
     * Called when game resumes from pause
     */
    protected abstract void onResume();
    
    /**
     * Called when game stops
     */
    protected abstract void onStop();
    
    /**
     * Save game-specific state data
     */
    protected abstract void saveStateData(GameState state);
    
    /**
     * Load game-specific state data
     */
    protected abstract void loadStateData(GameState state);
}
