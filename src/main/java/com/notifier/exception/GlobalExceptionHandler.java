package com.notifier.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Centralized exception handling is implemented within this component.
 * It intercepts specific exceptions and transforms them into standard JSON responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Logic errors and explicit illegal arguments are intercepted.
     *
     * @param ex The intercepted exception.
     * @param request The metadata of the current web request.
     * @return A formatted {@link ResponseEntity} with a 400 Bad Request status.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorMessage> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * JSON parsing errors, such as invalid Enum values, are intercepted here.
     *
     * @param ex The deserialization exception.
     * @param request The metadata of the current web request.
     * @return A formatted {@link ResponseEntity} explaining the JSON mapping failure.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMessage> handleInvalidJson(HttpMessageNotReadableException ex, WebRequest request) {
        String detail = "JSON Error: The provided notification type is invalid or the body format is incorrect.";
        return buildResponse(HttpStatus.BAD_REQUEST, detail, request);
    }

    /**
     * Validation errors triggered by @Valid annotations are intercepted.
     * This collects all field errors into a single readable string.
     *
     * @param ex The validation exception containing binding results.
     * @param request The metadata of the current web request.
     * @return A formatted {@link ResponseEntity} with detailed field errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed: " + errors, request);
    }

    /**
     * Any unhandled internal exceptions are captured to prevent stack trace leakage.
     *
     * @param ex The generic exception.
     * @param request The metadata of the current web request.
     * @return A formatted {@link ResponseEntity} with a 500 Internal Server Error status.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleGlobalException(Exception ex, WebRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred.", request);
    }

    /**
     * Strategy-level validation errors are intercepted here.
     * This bridges the gap between Domain Logic failures and HTTP responses.
     */
    @ExceptionHandler(InvalidNotificationException.class)
    public ResponseEntity<ErrorMessage> handleInvalidNotification(InvalidNotificationException ex, WebRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Internal helper method to maintain a consistent response structure.
     *
     * @param status The HTTP status to return.
     * @param message The error message to display.
     * @param request The web request context.
     * @return A wrapped {@link ErrorMessage} inside a {@link ResponseEntity}.
     */
    private ResponseEntity<ErrorMessage> buildResponse(HttpStatus status, String message, WebRequest request) {
        ErrorMessage error = new ErrorMessage(
                status.value(),
                LocalDateTime.now(),
                message,
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, status);
    }
}