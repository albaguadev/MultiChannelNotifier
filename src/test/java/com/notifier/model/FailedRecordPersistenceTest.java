package com.notifier.model;

import com.notifier.dto.NotificationRequest;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for {@link NotificationRecord} failure recording.
 * <p>
 * This test suite verifies that {@link NotificationRecord#ofFailure(NotificationRequest, String)}
 * correctly captures failure information including the error message.
 * </p>
 * <p>
 * Feature: notification-persistence<br>
 * Property 2: Registro de fallos de envío
 * </p>
 * <p>
 * Validates requirement: 1.2
 * </p>
 */
class FailedRecordPersistenceTest {

    /**
     * Property: When creating a failed notification record, the error message must be
     * preserved and the status must be FAILED.
     * <p>
     * Given: A valid NotificationRequest and an arbitrary error message<br>
     * When: Creating a NotificationRecord via ofFailure(request, errorMessage)<br>
     * Then: The record must have status FAILED and preserve the error message exactly
     * </p>
     */
    @Property(tries = 100)
    @Label("Feature: notification-persistence, Property 2: Registro de fallos de envío")
    void failedRecordPreservesErrorMessage(
            @ForAll("notificationRequests") NotificationRequest request,
            @ForAll("errorMessages") String errorMessage) {
        
        // When: Creating a failed notification record
        NotificationRecord record = NotificationRecord.ofFailure(request, errorMessage);

        // Then: Status is FAILED
        assertThat(record.getStatus())
                .as("Status must be FAILED for failed records")
                .isEqualTo(NotificationStatus.FAILED);

        // And: Error message is preserved
        assertThat(record.getErrorMessage())
                .as("Error message must be preserved exactly")
                .isEqualTo(errorMessage);

        // And: Request data is still preserved
        assertThat(record.getType())
                .as("Notification type must be preserved")
                .isEqualTo(request.getType());

        assertThat(record.getRecipient())
                .as("Recipient must be preserved")
                .isEqualTo(request.getRecipient());

        assertThat(record.getMessage())
                .as("Message must be preserved")
                .isEqualTo(request.getMessage());

        assertThat(record.getSubject())
                .as("Subject must be preserved")
                .isEqualTo(request.getSubject());

        // And: ID and timestamp are generated
        assertThat(record.getId())
                .as("ID must be generated")
                .isNotNull()
                .isNotBlank();

        assertThat(record.getTimestamp())
                .as("Timestamp must be generated")
                .isNotNull();
    }

    /**
     * Property: Failed records can be created with null error messages.
     * <p>
     * Given: A valid NotificationRequest and a null error message<br>
     * When: Creating a NotificationRecord via ofFailure(request, null)<br>
     * Then: The record must have status FAILED and errorMessage null
     * </p>
     */
    @Property(tries = 50)
    @Label("Feature: notification-persistence, Property 2: Registro de fallos con error null")
    void failedRecordAcceptsNullErrorMessage(
            @ForAll("notificationRequests") NotificationRequest request) {
        
        // When: Creating a failed notification record with null error message
        NotificationRecord record = NotificationRecord.ofFailure(request, null);

        // Then: Status is FAILED and error message is null
        assertThat(record.getStatus())
                .as("Status must be FAILED")
                .isEqualTo(NotificationStatus.FAILED);

        assertThat(record.getErrorMessage())
                .as("Error message can be null")
                .isNull();
    }

    /**
     * Provides arbitrary valid {@link NotificationRequest} instances.
     */
    @Provide
    Arbitrary<NotificationRequest> notificationRequests() {
        Arbitrary<NotificationType> types = Arbitraries.of(NotificationType.values());
        
        Arbitrary<String> recipients = Arbitraries.oneOf(
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20).map(s -> s + "@example.com"),
                Arbitraries.strings().numeric().ofLength(9).map(s -> "+34" + s)
        );

        Arbitrary<String> messages = Arbitraries.strings()
                .alpha().numeric().withChars(' ')
                .ofMinLength(1).ofMaxLength(500);

        Arbitrary<String> subjects = Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(100)
        );

        return Combinators.combine(types, recipients, messages, subjects)
                .as((type, recipient, message, subject) -> {
                    NotificationRequest request = new NotificationRequest();
                    request.setType(type);
                    request.setRecipient(recipient);
                    request.setMessage(message);
                    request.setSubject(subject);
                    return request;
                });
    }

    /**
     * Provides arbitrary error messages including various edge cases.
     */
    @Provide
    Arbitrary<String> errorMessages() {
        return Arbitraries.oneOf(
                // Common error messages
                Arbitraries.of(
                        "Connection timeout",
                        "Invalid recipient format",
                        "Service unavailable",
                        "Authentication failed",
                        "Rate limit exceeded"
                ),
                // Random alphanumeric messages
                Arbitraries.strings()
                        .alpha().numeric().withChars(' ', ':', '-', '.')
                        .ofMinLength(5).ofMaxLength(200),
                // Empty string (valid error message)
                Arbitraries.just("")
        );
    }
}
