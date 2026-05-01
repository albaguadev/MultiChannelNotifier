package com.notifier.model;

import com.notifier.dto.NotificationRequest;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for {@link NotificationRecord} round-trip data preservation.
 * <p>
 * This test suite verifies that {@link NotificationRecord#ofSuccess(NotificationRequest)}
 * correctly preserves all input data from the request without loss or transformation.
 * </p>
 * <p>
 * Feature: notification-persistence<br>
 * Property 1: Round-trip de datos en envío exitoso
 * </p>
 * <p>
 * Validates requirements: 1.1, 5.1, 5.2
 * </p>
 */
class NotificationRecordRoundTripTest {

    /**
     * Property: When creating a successful notification record, all data from the request
     * must be preserved exactly as provided.
     * <p>
     * Given: A valid NotificationRequest with arbitrary type, recipient, message, and subject<br>
     * When: Creating a NotificationRecord via ofSuccess(request)<br>
     * Then: The record must preserve type, recipient, message, and subject exactly,
     *       and status must be SENT
     * </p>
     */
    @Property(tries = 100)
    @Label("Feature: notification-persistence, Property 1: Round-trip de datos en envío exitoso")
    void successfulRecordPreservesAllRequestData(
            @ForAll("notificationRequests") NotificationRequest request) {
        
        // When: Creating a successful notification record
        NotificationRecord record = NotificationRecord.ofSuccess(request);

        // Then: All data from the request is preserved
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
                .as("Subject must be preserved (including null)")
                .isEqualTo(request.getSubject());

        assertThat(record.getStatus())
                .as("Status must be SENT for successful records")
                .isEqualTo(NotificationStatus.SENT);

        assertThat(record.getErrorMessage())
                .as("Error message must be null for successful records")
                .isNull();

        assertThat(record.getId())
                .as("ID must be generated")
                .isNotNull()
                .isNotBlank();

        assertThat(record.getTimestamp())
                .as("Timestamp must be generated")
                .isNotNull();
    }

    /**
     * Provides arbitrary valid {@link NotificationRequest} instances for property-based testing.
     * <p>
     * Generates requests with:
     * <ul>
     *   <li>All three notification types (EMAIL, SMS, WHATSAPP)</li>
     *   <li>Valid recipient addresses (email or phone format)</li>
     *   <li>Non-empty messages</li>
     *   <li>Optional subjects (50% null, 50% non-empty string)</li>
     * </ul>
     * </p>
     */
    @Provide
    Arbitrary<NotificationRequest> notificationRequests() {
        Arbitrary<NotificationType> types = Arbitraries.of(NotificationType.values());
        
        Arbitrary<String> recipients = Arbitraries.oneOf(
                // Email addresses
                Arbitraries.strings()
                        .alpha()
                        .ofMinLength(3)
                        .ofMaxLength(20)
                        .map(s -> s + "@example.com"),
                // Phone numbers
                Arbitraries.strings()
                        .numeric()
                        .ofLength(9)
                        .map(s -> "+34" + s)
        );

        Arbitrary<String> messages = Arbitraries.strings()
                .alpha()
                .numeric()
                .withChars(' ', '.', ',', '!', '?')
                .ofMinLength(1)
                .ofMaxLength(500);

        Arbitrary<String> subjects = Arbitraries.oneOf(
                // null subjects
                Arbitraries.just(null),
                // non-empty subjects
                Arbitraries.strings()
                        .alpha()
                        .numeric()
                        .withChars(' ')
                        .ofMinLength(1)
                        .ofMaxLength(100)
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
}
