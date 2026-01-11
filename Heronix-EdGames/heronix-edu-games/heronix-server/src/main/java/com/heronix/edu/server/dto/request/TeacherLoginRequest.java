package com.heronix.edu.server.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for teacher/admin login.
 */
public record TeacherLoginRequest(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password
) {
}
