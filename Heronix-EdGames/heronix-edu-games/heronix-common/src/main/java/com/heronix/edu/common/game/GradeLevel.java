package com.heronix.edu.common.game;

/**
 * Grade levels for educational content
 */
public enum GradeLevel {
    KINDERGARTEN("K", "Kindergarten"),
    FIRST("1", "First Grade"),
    SECOND("2", "Second Grade"),
    THIRD("3", "Third Grade"),
    FOURTH("4", "Fourth Grade"),
    FIFTH("5", "Fifth Grade"),
    SIXTH("6", "Sixth Grade"),
    SEVENTH("7", "Seventh Grade"),
    EIGHTH("8", "Eighth Grade"),
    NINTH("9", "Ninth Grade"),
    TENTH("10", "Tenth Grade"),
    ELEVENTH("11", "Eleventh Grade"),
    TWELFTH("12", "Twelfth Grade"),
    ALL("ALL", "All Grades");
    
    private final String code;
    private final String displayName;
    
    GradeLevel(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static GradeLevel fromCode(String code) {
        for (GradeLevel level : values()) {
            if (level.code.equals(code)) {
                return level;
            }
        }
        return null;
    }
}
