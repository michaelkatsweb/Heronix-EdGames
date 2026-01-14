package com.heronix.edu.server.dto.game;

import java.time.LocalDateTime;

/**
 * DTO representing a player in a game session.
 */
public class PlayerDto {
    private String playerId;
    private String studentId;
    private String studentName;
    private String avatarId;
    private Integer credits;
    private Integer correctAnswers;
    private Integer incorrectAnswers;
    private Double accuracy;
    private Integer hackAttempts;
    private Integer successfulHacks;
    private Double hackSuccessRate;
    private Integer timesHacked;
    private Integer creditsStolen;
    private Integer creditsLost;
    private Boolean connected;
    private LocalDateTime joinedAt;
    private Integer rank;

    public PlayerDto() {}

    // Getters and Setters
    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

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

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public Integer getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Integer correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Integer getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public void setIncorrectAnswers(Integer incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Integer getHackAttempts() {
        return hackAttempts;
    }

    public void setHackAttempts(Integer hackAttempts) {
        this.hackAttempts = hackAttempts;
    }

    public Integer getSuccessfulHacks() {
        return successfulHacks;
    }

    public void setSuccessfulHacks(Integer successfulHacks) {
        this.successfulHacks = successfulHacks;
    }

    public Double getHackSuccessRate() {
        return hackSuccessRate;
    }

    public void setHackSuccessRate(Double hackSuccessRate) {
        this.hackSuccessRate = hackSuccessRate;
    }

    public Integer getTimesHacked() {
        return timesHacked;
    }

    public void setTimesHacked(Integer timesHacked) {
        this.timesHacked = timesHacked;
    }

    public Integer getCreditsStolen() {
        return creditsStolen;
    }

    public void setCreditsStolen(Integer creditsStolen) {
        this.creditsStolen = creditsStolen;
    }

    public Integer getCreditsLost() {
        return creditsLost;
    }

    public void setCreditsLost(Integer creditsLost) {
        this.creditsLost = creditsLost;
    }

    public Boolean getConnected() {
        return connected;
    }

    public void setConnected(Boolean connected) {
        this.connected = connected;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}
