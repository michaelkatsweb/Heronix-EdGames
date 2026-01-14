package com.heronix.edu.server.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for student play time report
 * Used by teachers to track student engagement
 */
public class StudentPlayTimeReport {
    private String studentId;
    private String studentName;
    private String gradeLevel;
    private String deviceId;
    private int totalPlayTimeMinutes;
    private int totalSessions;
    private int totalGamesPlayed;
    private double averageSessionMinutes;
    private double averageScore;
    private LocalDate firstPlayDate;
    private LocalDate lastPlayDate;
    private List<GamePlaySummary> gameBreakdown;

    // Constructors
    public StudentPlayTimeReport() {
    }

    public StudentPlayTimeReport(String studentId, String studentName) {
        this.studentId = studentId;
        this.studentName = studentName;
    }

    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getTotalPlayTimeMinutes() {
        return totalPlayTimeMinutes;
    }

    public void setTotalPlayTimeMinutes(int totalPlayTimeMinutes) {
        this.totalPlayTimeMinutes = totalPlayTimeMinutes;
    }

    public int getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(int totalSessions) {
        this.totalSessions = totalSessions;
    }

    public int getTotalGamesPlayed() {
        return totalGamesPlayed;
    }

    public void setTotalGamesPlayed(int totalGamesPlayed) {
        this.totalGamesPlayed = totalGamesPlayed;
    }

    public double getAverageSessionMinutes() {
        return averageSessionMinutes;
    }

    public void setAverageSessionMinutes(double averageSessionMinutes) {
        this.averageSessionMinutes = averageSessionMinutes;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public LocalDate getFirstPlayDate() {
        return firstPlayDate;
    }

    public void setFirstPlayDate(LocalDate firstPlayDate) {
        this.firstPlayDate = firstPlayDate;
    }

    public LocalDate getLastPlayDate() {
        return lastPlayDate;
    }

    public void setLastPlayDate(LocalDate lastPlayDate) {
        this.lastPlayDate = lastPlayDate;
    }

    public List<GamePlaySummary> getGameBreakdown() {
        return gameBreakdown;
    }

    public void setGameBreakdown(List<GamePlaySummary> gameBreakdown) {
        this.gameBreakdown = gameBreakdown;
    }

    /**
     * Inner class for per-game breakdown
     */
    public static class GamePlaySummary {
        private String gameId;
        private String gameName;
        private int playTimeMinutes;
        private int sessionsPlayed;
        private double averageScore;
        private int highScore;

        public GamePlaySummary() {
        }

        public GamePlaySummary(String gameId, String gameName) {
            this.gameId = gameId;
            this.gameName = gameName;
        }

        public String getGameId() {
            return gameId;
        }

        public void setGameId(String gameId) {
            this.gameId = gameId;
        }

        public String getGameName() {
            return gameName;
        }

        public void setGameName(String gameName) {
            this.gameName = gameName;
        }

        public int getPlayTimeMinutes() {
            return playTimeMinutes;
        }

        public void setPlayTimeMinutes(int playTimeMinutes) {
            this.playTimeMinutes = playTimeMinutes;
        }

        public int getSessionsPlayed() {
            return sessionsPlayed;
        }

        public void setSessionsPlayed(int sessionsPlayed) {
            this.sessionsPlayed = sessionsPlayed;
        }

        public double getAverageScore() {
            return averageScore;
        }

        public void setAverageScore(double averageScore) {
            this.averageScore = averageScore;
        }

        public int getHighScore() {
            return highScore;
        }

        public void setHighScore(int highScore) {
            this.highScore = highScore;
        }
    }
}
