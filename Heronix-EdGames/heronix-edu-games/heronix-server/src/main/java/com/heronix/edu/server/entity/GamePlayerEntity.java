package com.heronix.edu.server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a player in a multiplayer game session.
 * Tracks player stats, secret code, and current game state.
 */
@Entity
@Table(name = "game_players")
public class GamePlayerEntity {

    @Id
    @Column(name = "player_id", length = 50)
    private String playerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private GameSessionEntity session;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "secret_code", length = 10)
    private String secretCode;  // e.g., "1234" for Code Breaker

    @Column(name = "avatar_id", length = 50)
    private String avatarId;

    @Column(name = "credits")
    private Integer credits = 0;

    @Column(name = "correct_answers")
    private Integer correctAnswers = 0;

    @Column(name = "incorrect_answers")
    private Integer incorrectAnswers = 0;

    @Column(name = "hack_attempts")
    private Integer hackAttempts = 0;

    @Column(name = "successful_hacks")
    private Integer successfulHacks = 0;

    @Column(name = "times_hacked")
    private Integer timesHacked = 0;

    @Column(name = "credits_stolen")
    private Integer creditsStolen = 0;  // Total credits stolen from others

    @Column(name = "credits_lost")
    private Integer creditsLost = 0;    // Total credits lost to hackers

    @Column(name = "connected")
    private Boolean connected = true;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    // Constructors
    public GamePlayerEntity() {
        this.joinedAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
    }

    public GamePlayerEntity(String playerId, String studentId, String studentName) {
        this();
        this.playerId = playerId;
        this.studentId = studentId;
        this.studentName = studentName;
    }

    // Game action methods
    public void incrementCorrectAnswers() {
        this.correctAnswers++;
        this.lastActivityAt = LocalDateTime.now();
    }

    public void incrementIncorrectAnswers() {
        this.incorrectAnswers++;
        this.lastActivityAt = LocalDateTime.now();
    }

    public void incrementHackAttempts() {
        this.hackAttempts++;
        this.lastActivityAt = LocalDateTime.now();
    }

    public void incrementSuccessfulHacks() {
        this.successfulHacks++;
    }

    public void incrementTimesHacked() {
        this.timesHacked++;
    }

    public void addCredits(int amount) {
        this.credits += amount;
    }

    public void subtractCredits(int amount) {
        this.credits = Math.max(0, this.credits - amount);
    }

    public void recordCreditsStolen(int amount) {
        this.creditsStolen += amount;
    }

    public void recordCreditsLost(int amount) {
        this.creditsLost += amount;
    }

    public double getAccuracy() {
        int total = correctAnswers + incorrectAnswers;
        if (total == 0) return 0.0;
        return (double) correctAnswers / total * 100.0;
    }

    public double getHackSuccessRate() {
        if (hackAttempts == 0) return 0.0;
        return (double) successfulHacks / hackAttempts * 100.0;
    }

    // Getters and Setters
    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public GameSessionEntity getSession() {
        return session;
    }

    public void setSession(GameSessionEntity session) {
        this.session = session;
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

    public String getSecretCode() {
        return secretCode;
    }

    public void setSecretCode(String secretCode) {
        this.secretCode = secretCode;
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

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }
}
