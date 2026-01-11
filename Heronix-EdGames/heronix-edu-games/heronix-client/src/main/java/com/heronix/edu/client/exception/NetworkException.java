package com.heronix.edu.client.exception;

/**
 * Exception thrown when network operations fail
 */
public class NetworkException extends ClientException {

    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
