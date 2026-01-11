package com.heronix.edu.client.api.dto;

import java.util.List;

/**
 * Request DTO for uploading game scores
 */
public class SyncUploadRequest {
    private String deviceId;
    private List<GameScoreDto> scores;

    public SyncUploadRequest() {
    }

    public SyncUploadRequest(String deviceId, List<GameScoreDto> scores) {
        this.deviceId = deviceId;
        this.scores = scores;
    }

    // Getters and Setters
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<GameScoreDto> getScores() {
        return scores;
    }

    public void setScores(List<GameScoreDto> scores) {
        this.scores = scores;
    }
}
