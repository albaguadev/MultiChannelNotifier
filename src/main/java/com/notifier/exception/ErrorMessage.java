package com.notifier.exception;

import java.time.LocalDateTime;

/**
 * A standardized structure is provided for API error responses.
 * This ensures that clients receive consistent metadata during failure scenarios.
 */
public record ErrorMessage(
        int status,
        LocalDateTime timestamp,
        String message,
        String description
) {}