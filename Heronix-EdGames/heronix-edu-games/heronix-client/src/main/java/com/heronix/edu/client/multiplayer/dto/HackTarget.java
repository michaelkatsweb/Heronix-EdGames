package com.heronix.edu.client.multiplayer.dto;

/**
 * Represents a potential hack target (another player).
 */
public class HackTarget {
    private String playerId;
    private String studentName;
    private String avatarId;
    private int credits;
    private boolean shielded;
    private String codeHint; // Partial hint of secret code (if revealed)

    public HackTarget() {}

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getAvatarId() { return avatarId; }
    public void setAvatarId(String avatarId) { this.avatarId = avatarId; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public boolean isShielded() { return shielded; }
    public void setShielded(boolean shielded) { this.shielded = shielded; }

    public String getCodeHint() { return codeHint; }
    public void setCodeHint(String codeHint) { this.codeHint = codeHint; }

    /**
     * Get potential steal amount (50% of credits).
     */
    public int getPotentialSteal() {
        return credits / 2;
    }
}
