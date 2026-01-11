package com.heronix.edu.common.game;

/**
 * Game difficulty levels
 */
public enum DifficultyLevel {
    EASY("Easy", 1),
    MEDIUM("Medium", 2),
    HARD("Hard", 3),
    EXPERT("Expert", 4);
    
    private final String displayName;
    private final int numericLevel;
    
    DifficultyLevel(String displayName, int numericLevel) {
        this.displayName = displayName;
        this.numericLevel = numericLevel;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getNumericLevel() {
        return numericLevel;
    }
}
