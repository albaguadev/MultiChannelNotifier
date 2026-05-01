package com.notifier.exception;

/**
 * Custom exception thrown when an error occurs while interacting
 * with the persistence layer (e.g., saving or querying notification records).
 *
 * <p>This is an unchecked exception that wraps lower-level infrastructure
 * failures, allowing callers to handle persistence errors without being
 * forced to declare checked exceptions.</p>
 */
public class PersistenceException extends RuntimeException {

    /**
     * Constructs a new {@code PersistenceException} with the specified detail
     * message and root cause.
     *
     * @param message a human-readable description of the error
     * @param cause   the underlying exception that triggered this failure
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
