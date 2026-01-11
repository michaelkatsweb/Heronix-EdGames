package com.heronix.edu.client.api.exception;

/**
 * Exception thrown for network connectivity issues
 */
public class NetworkException extends RuntimeException {

    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
