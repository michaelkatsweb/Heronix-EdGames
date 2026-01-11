package com.heronix.edu.server.dto.response;

/**
 * Types of sync conflicts that can occur.
 */
public enum ConflictType {
    DUPLICATE_SCORE,
    VALIDATION_ERROR,
    STUDENT_MISMATCH,
    DEVICE_NOT_AUTHORIZED
}
