package com.heronix.edu.server.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for approving a device and assigning it to a student
 */
public record DeviceApprovalRequest(
        @JsonProperty("studentId")
        @NotBlank(message = "Student ID is required for device approval")
        String studentId
) {
}
