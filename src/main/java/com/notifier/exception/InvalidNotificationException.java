package com.notifier.exception;

/**
 * Custom exception thrown when a notification request fails
 * validation at the strategy level.
 */
public class InvalidNotificationException extends RuntimeException {

    public InvalidNotificationException(String message) {
        super(message);
    }

    public InvalidNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}