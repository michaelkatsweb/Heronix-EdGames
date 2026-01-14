package com.heronix.edu.client.multiplayer.dto;

/**
 * Represents the current state of the player in the Code Breaker game.
 */
public class PlayerState {
    private String playerId;
    private String studentName;
    private String avatarId;
    private String secretCode;
    private int credits;
    private int correctAnswers;
    private int wrongAnswers;
    private int hackAttempts;
    private int successfulHacks;
    private int timesHacked;
    private boolean shielded;
    private int rank;

    public PlayerState() {}

    // Getters and Setters
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    // Alias for playerName (same as studentName)
    public String getPlayerName() { return studentName; }
    public void setPlayerName(String playerName) { this.studentName = playerName; }

    public String getAvatarId() { return avatarId; }
    public void setAvatarId(String avatarId) { this.avatarId = avatarId; }

    public String getSecretCode() { return secretCode; }
    public void setSecretCode(String secretCode) { this.secretCode = secretCode; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public int getWrongAnswers() { return wrongAnswers; }
    public void setWrongAnswers(int wrongAnswers) { this.wrongAnswers = wrongAnswers; }

    public int getHackAttempts() { return hackAttempts; }
    public void setHackAttempts(int hackAttempts) { this.hackAttempts = hackAttempts; }

    public int getSuccessfulHacks() { return successfulHacks; }
    public void setSuccessfulHacks(int successfulHacks) { this.successfulHacks = successfulHacks; }

    public int getTimesHacked() { return timesHacked; }
    public void setTimesHacked(int timesHacked) { this.timesHacked = timesHacked; }

    public boolean isShielded() { return shielded; }
    public void setShielded(boolean shielded) { this.shielded = shielded; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public double getAccuracy() {
        int total = correctAnswers + wrongAnswers;
        return total > 0 ? (correctAnswers * 100.0) / total : 0;
    }
}
