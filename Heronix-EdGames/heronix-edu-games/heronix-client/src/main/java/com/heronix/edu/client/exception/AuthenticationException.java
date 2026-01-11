package com.heronix.edu.client.exception;

/**
 * Exception thrown when authentication fails
 */
public class AuthenticationException extends ClientException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
