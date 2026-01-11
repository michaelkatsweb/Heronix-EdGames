package com.heronix.edu.common.game;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents saveable game state for pause/resume functionality
 */
public class GameState implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final String gameId;
    private final LocalDateTime savedAt;
    private final Map<String, Object> stateData;
    private int currentScore;
    private int currentQuestion;
    private int timeElapsed;
    private DifficultyLevel difficultyLevel;
    
    public GameState(String gameId) {
        this.gameId = gameId;
        this.savedAt = LocalDateTime.now();
        this.stateData = new HashMap<>();
        this.currentScore = 0;
        this.currentQuestion = 0;
        this.timeElapsed = 0;
    }
    
    public String getGameId() {
        return gameId;
    }
    
    public LocalDateTime getSavedAt() {
        return savedAt;
    }
    
    public Map<String, Object> getStateData() {
        return stateData;
    }
    
    public void putData(String key, Object value) {
        stateData.put(key, value);
    }
    
    public Object getData(String key) {
        return stateData.get(key);
    }
    
    public <T> T getData(String key, Class<T> type) {
        Object value = stateData.get(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }
    
    public int getCurrentScore() {
        return currentScore;
    }
    
    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }
    
    public int getCurrentQuestion() {
        return currentQuestion;
    }
    
    public void setCurrentQuestion(int currentQuestion) {
        this.currentQuestion = currentQuestion;
    }
    
    public int getTimeElapsed() {
        return timeElapsed;
    }
    
    public void setTimeElapsed(int timeElapsed) {
        this.timeElapsed = timeElapsed;
    }
    
    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }
    
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
}
