/**
 * Global exception handling and error response mapping.
 * <p>
 * This package provides a centralized interceptor ({@code @RestControllerAdvice})
 * that captures application-wide exceptions. It ensures that all failure scenarios
 * are transformed into standardized {@code ErrorMessage} JSON payloads,
 * maintaining a consistent API contract and preventing internal stack trace leakage.
 * </p>
 * @auhtor albaguadev
 * @version 1.1.0-SNAPSHOT
 */
package com.notifier.exception;