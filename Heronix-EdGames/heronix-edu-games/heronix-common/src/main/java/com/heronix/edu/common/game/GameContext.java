package com.heronix.edu.common.game;

import com.heronix.edu.common.model.Student;
import java.util.HashMap;
import java.util.Map;

/**
 * Context provided to games containing student info and settings
 */
public class GameContext {
    
    private final Student student;
    private final String deviceId;
    private DifficultyLevel difficultyLevel;
    private final Map<String, Object> settings;
    private boolean soundEnabled;
    private boolean musicEnabled;
    private int timeLimit; // in seconds, 0 = no limit
    
    public GameContext(Student student, String deviceId) {
        this.student = student;
        this.deviceId = deviceId;
        this.settings = new HashMap<>();
        this.difficultyLevel = DifficultyLevel.MEDIUM;
        this.soundEnabled = true;
        this.musicEnabled = true;
        this.timeLimit = 0;
    }
    
    public Student getStudent() {
        return student;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }
    
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
    
    public Map<String, Object> getSettings() {
        return settings;
    }
    
    public void setSetting(String key, Object value) {
        settings.put(key, value);
    }
    
    public Object getSetting(String key) {
        return settings.get(key);
    }
    
    public <T> T getSetting(String key, Class<T> type, T defaultValue) {
        Object value = settings.get(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return defaultValue;
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }
    
    public boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    public void setMusicEnabled(boolean musicEnabled) {
        this.musicEnabled = musicEnabled;
    }
    
    public int getTimeLimit() {
        return timeLimit;
    }
    
    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }
}
