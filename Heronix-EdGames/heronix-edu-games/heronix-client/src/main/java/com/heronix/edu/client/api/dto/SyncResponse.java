package com.heronix.edu.client.api.dto;

import java.util.List;

/**
 * Response DTO for sync operations
 */
public class SyncResponse {
    private boolean success;
    private int successCount;
    private int failureCount;
    private List<String> errors;
    private String message;

    public SyncResponse() {
    }

    /**
     * Create a successful sync response.
     */
    public static SyncResponse success(int successCount) {
        SyncResponse response = new SyncResponse();
        response.setSuccess(true);
        response.setSuccessCount(successCount);
        response.setFailureCount(0);
        response.setMessage("Sync completed successfully");
        return response;
    }

    /**
     * Create a failed sync response.
     */
    public static SyncResponse failure(String errorMessage) {
        SyncResponse response = new SyncResponse();
        response.setSuccess(false);
        response.setSuccessCount(0);
        response.setFailureCount(1);
        response.setMessage(errorMessage);
        response.setErrors(List.of(errorMessage));
        return response;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
