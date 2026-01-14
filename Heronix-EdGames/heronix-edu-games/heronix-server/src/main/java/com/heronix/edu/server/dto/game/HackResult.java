package com.heronix.edu.server.dto.game;

/**
 * Result of a hack attempt.
 */
public class HackResult {
    private boolean success;
    private String targetPlayerName;
    private Integer creditsStolen;
    private Integer newTotalCredits;
    private String hint;           // Hint shown after failed attempts
    private Integer failedAttempts; // How many times failed on this target
    private String challengeType;  // Mini-challenge for hacked player

    public HackResult() {}

    public static HackResult success(String targetPlayerName, int creditsStolen, int newTotal) {
        HackResult result = new HackResult();
        result.success = true;
        result.targetPlayerName = targetPlayerName;
        result.creditsStolen = creditsStolen;
        result.newTotalCredits = newTotal;
        return result;
    }

    public static HackResult failure(String hint, int failedAttempts) {
        HackResult result = new HackResult();
        result.success = false;
        result.hint = hint;
        result.failedAttempts = failedAttempts;
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getTargetPlayerName() {
        return targetPlayerName;
    }

    public void setTargetPlayerName(String targetPlayerName) {
        this.targetPlayerName = targetPlayerName;
    }

    public Integer getCreditsStolen() {
        return creditsStolen;
    }

    public void setCreditsStolen(Integer creditsStolen) {
        this.creditsStolen = creditsStolen;
    }

    public Integer getNewTotalCredits() {
        return newTotalCredits;
    }

    public void setNewTotalCredits(Integer newTotalCredits) {
        this.newTotalCredits = newTotalCredits;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public Integer getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(Integer failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public String getChallengeType() {
        return challengeType;
    }

    public void setChallengeType(String challengeType) {
        this.challengeType = challengeType;
    }
}
