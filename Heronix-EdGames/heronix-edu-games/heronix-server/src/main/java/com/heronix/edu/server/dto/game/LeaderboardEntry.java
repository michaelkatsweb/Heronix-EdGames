package com.heronix.edu.server.dto.game;

/**
 * Entry in the game leaderboard.
 */
public class LeaderboardEntry {
    private Integer rank;
    private String playerId;
    private String studentName;
    private String avatarId;
    private Integer credits;
    private Integer correctAnswers;
    private Integer successfulHacks;
    private Boolean isCurrentPlayer;  // Flag if this is the requesting player

    public LeaderboardEntry() {}

    public LeaderboardEntry(int rank, String playerId, String studentName, String avatarId, int credits) {
        this.rank = rank;
        this.playerId = playerId;
        this.studentName = studentName;
        this.avatarId = avatarId;
        this.credits = credits;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
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

    public Integer getSuccessfulHacks() {
        return successfulHacks;
    }

    public void setSuccessfulHacks(Integer successfulHacks) {
        this.successfulHacks = successfulHacks;
    }

    public Boolean getIsCurrentPlayer() {
        return isCurrentPlayer;
    }

    public void setIsCurrentPlayer(Boolean isCurrentPlayer) {
        this.isCurrentPlayer = isCurrentPlayer;
    }
}
