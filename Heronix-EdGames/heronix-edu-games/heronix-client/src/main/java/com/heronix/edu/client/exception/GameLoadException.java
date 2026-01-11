package com.heronix.edu.client.exception;

/**
 * Exception thrown when game loading fails
 */
public class GameLoadException extends ClientException {

    public GameLoadException(String message) {
        super(message);
    }

    public GameLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
