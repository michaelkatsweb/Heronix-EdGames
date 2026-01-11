package com.heronix.edu.common.game;

/**
 * Educational subject areas
 */
public enum GameSubject {
    MATHEMATICS("Mathematics"),
    LANGUAGE_ARTS("Language Arts"),
    SCIENCE("Science"),
    SOCIAL_STUDIES("Social Studies"),
    GEOGRAPHY("Geography"),
    HISTORY("History"),
    COMPUTER_SCIENCE("Computer Science"),
    ART("Art"),
    MUSIC("Music"),
    PHYSICAL_EDUCATION("Physical Education"),
    GENERAL("General");
    
    private final String displayName;
    
    GameSubject(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
