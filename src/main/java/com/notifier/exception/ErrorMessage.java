package com.notifier.exception;

import java.time.LocalDateTime;

/**
 * A standardized structure is provided for API error responses.
 * * @param status The HTTP status code value.
 * @param timestamp The exact moment the error occurred.
 * @param message A brief summary of the error.
 * @param description Detailed context or URI of the request.
 */
public record ErrorMessage(
        int status,
        LocalDateTime timestamp,
        String message,
        String description
) {}