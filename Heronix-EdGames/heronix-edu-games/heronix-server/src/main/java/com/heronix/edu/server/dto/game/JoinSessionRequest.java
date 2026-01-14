package com.heronix.edu.server.dto.game;

/**
 * Request from student to join a game session.
 */
public class JoinSessionRequest {
    private String studentId;
    private String studentName;
    private String secretCode;  // Player's chosen secret code (e.g., "1234")
    private String avatarId;    // Selected avatar

    public JoinSessionRequest() {}

    public JoinSessionRequest(String studentId, String studentName, String secretCode, String avatarId) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.secretCode = secretCode;
        this.avatarId = avatarId;
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
}
