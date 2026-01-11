package com.heronix.edu.server.exception;

/**
 * Exception thrown when a registration code is invalid, expired, or exhausted.
 */
public class InvalidRegistrationCodeException extends RuntimeException {

    public InvalidRegistrationCodeException(String message) {
        super(message);
    }

    public InvalidRegistrationCodeException(String code, String reason) {
        super(String.format("Registration code '%s' is invalid: %s", code, reason));
    }
}
