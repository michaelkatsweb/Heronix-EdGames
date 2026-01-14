package com.heronix.edu.server.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for class-wide play time report
 * Used by teachers and administrators for overview
 */
public class ClassPlayTimeReport {
    private String classId;
    private String className;
    private String teacherName;
    private LocalDate reportStartDate;
    private LocalDate reportEndDate;
    private int totalStudents;
    private int activeStudents;
    private int totalPlayTimeMinutes;
    private int totalSessions;
    private double averagePlayTimePerStudent;
    private double averageScorePercentage;
    private List<StudentPlayTimeReport> studentReports;
    private List<GameUsageSummary> gameUsage;

    // Constructors
    public ClassPlayTimeReport() {
    }

    // Getters and Setters
    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public LocalDate getReportStartDate() {
        return reportStartDate;
    }

    public void setReportStartDate(LocalDate reportStartDate) {
        this.reportStartDate = reportStartDate;
    }

    public LocalDate getReportEndDate() {
        return reportEndDate;
    }

    public void setReportEndDate(LocalDate reportEndDate) {
        this.reportEndDate = reportEndDate;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public int getActiveStudents() {
        return activeStudents;
    }

    public void setActiveStudents(int activeStudents) {
        this.activeStudents = activeStudents;
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

    public double getAveragePlayTimePerStudent() {
        return averagePlayTimePerStudent;
    }

    public void setAveragePlayTimePerStudent(double averagePlayTimePerStudent) {
        this.averagePlayTimePerStudent = averagePlayTimePerStudent;
    }

    public double getAverageScorePercentage() {
        return averageScorePercentage;
    }

    public void setAverageScorePercentage(double averageScorePercentage) {
        this.averageScorePercentage = averageScorePercentage;
    }

    public List<StudentPlayTimeReport> getStudentReports() {
        return studentReports;
    }

    public void setStudentReports(List<StudentPlayTimeReport> studentReports) {
        this.studentReports = studentReports;
    }

    public List<GameUsageSummary> getGameUsage() {
        return gameUsage;
    }

    public void setGameUsage(List<GameUsageSummary> gameUsage) {
        this.gameUsage = gameUsage;
    }

    /**
     * Inner class for game usage statistics
     */
    public static class GameUsageSummary {
        private String gameId;
        private String gameName;
        private String subject;
        private int totalPlayTimeMinutes;
        private int uniquePlayers;
        private int totalSessions;
        private double averageScore;

        public GameUsageSummary() {
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

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public int getTotalPlayTimeMinutes() {
            return totalPlayTimeMinutes;
        }

        public void setTotalPlayTimeMinutes(int totalPlayTimeMinutes) {
            this.totalPlayTimeMinutes = totalPlayTimeMinutes;
        }

        public int getUniquePlayers() {
            return uniquePlayers;
        }

        public void setUniquePlayers(int uniquePlayers) {
            this.uniquePlayers = uniquePlayers;
        }

        public int getTotalSessions() {
            return totalSessions;
        }

        public void setTotalSessions(int totalSessions) {
            this.totalSessions = totalSessions;
        }

        public double getAverageScore() {
            return averageScore;
        }

        public void setAverageScore(double averageScore) {
            this.averageScore = averageScore;
        }
    }
}
