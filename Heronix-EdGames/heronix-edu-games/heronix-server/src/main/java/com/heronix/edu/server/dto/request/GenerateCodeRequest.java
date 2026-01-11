package com.heronix.edu.server.dto.request;

/**
 * Request DTO for generating registration codes.
 */
public record GenerateCodeRequest(
        Long classId,        // Optional - can be null for school-wide codes
        Integer maxUses,     // Optional - null means unlimited
        Integer validDays    // Optional - null means no expiration
) {
}
