package com.heronix.edu.client.multiplayer.dto;

/**
 * Represents a player entry on the leaderboard.
 */
public class LeaderboardPlayer {
    private int rank;
    private String playerId;
    private String studentName;
    private String avatarId;
    private int credits;
    private int correctAnswers;
    private int wrongAnswers;
    private boolean shielded;

    public LeaderboardPlayer() {}

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getAvatarId() { return avatarId; }
    public void setAvatarId(String avatarId) { this.avatarId = avatarId; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public int getWrongAnswers() { return wrongAnswers; }
    public void setWrongAnswers(int wrongAnswers) { this.wrongAnswers = wrongAnswers; }

    public boolean isShielded() { return shielded; }
    public void setShielded(boolean shielded) { this.shielded = shielded; }

    public double getAccuracy() {
        int total = correctAnswers + wrongAnswers;
        return total > 0 ? (correctAnswers * 100.0) / total : 0;
    }
}
