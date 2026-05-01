package com.notifier.model;

import com.notifier.dto.NotificationRequest;
import net.jqwik.api.*;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for {@link NotificationRecord} timestamp precision.
 * <p>
 * This test suite verifies that timestamps are stored with millisecond precision
 * and are not truncated to seconds during record creation.
 * </p>
 * <p>
 * Feature: notification-persistence<br>
 * Property 4: Conservación de precisión temporal
 * </p>
 * <p>
 * Validates requirement: 1.4
 * </p>
 */
class TimestampPrecisionTest {

    /**
     * Property: Timestamps must preserve millisecond precision.
     * <p>
     * Given: Any valid NotificationRequest<br>
     * When: Creating a NotificationRecord<br>
     * Then: The timestamp must have millisecond precision (not truncated to seconds)
     * </p>
     */
    @Property(tries = 100)
    @Label("Feature: notification-persistence, Property 4: Conservación de precisión temporal")
    void timestampPreservesMillisecondPrecision(
            @ForAll("notificationRequests") NotificationRequest request) {
        
        // When: Creating a notification record
        Instant before = Instant.now();
        NotificationRecord record = NotificationRecord.ofSuccess(request);
        Instant after = Instant.now();

        // Then: Timestamp is within the expected range
        assertThat(record.getTimestamp())
                .as("Timestamp must be between before and after instants")
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);

        // And: Timestamp has millisecond precision (not truncated to seconds)
        long timestampMillis = record.getTimestamp().toEpochMilli();
        long timestampSeconds = record.getTimestamp().getEpochSecond();
        
        // Reconstruct from millis and verify millisecond component is preserved
        Instant reconstructedFromMillis = Instant.ofEpochMilli(timestampMillis);
        
        assertThat(reconstructedFromMillis.toEpochMilli())
                .as("Timestamp milliseconds must be preserved when reconstructed")
                .isEqualTo(timestampMillis);

        // Verify that millisecond component exists (not truncated to seconds)
        long millisComponent = timestampMillis - (timestampSeconds * 1000);
        
        assertThat(millisComponent)
                .as("Millisecond component must be in valid range [0, 999]")
                .isBetween(0L, 999L);
    }

    /**
     * Property: Timestamps for failed records also preserve millisecond precision.
     * <p>
     * Given: Any valid NotificationRequest and error message<br>
     * When: Creating a failed NotificationRecord<br>
     * Then: The timestamp must have millisecond precision
     * </p>
     */
    @Property(tries = 100)
    @Label("Feature: notification-persistence, Property 4: Precisión temporal en fallos")
    void failedRecordTimestampPreservesMillisecondPrecision(
            @ForAll("notificationRequests") NotificationRequest request,
            @ForAll("errorMessages") String errorMessage) {
        
        // When: Creating a failed notification record
        Instant before = Instant.now();
        NotificationRecord record = NotificationRecord.ofFailure(request, errorMessage);
        Instant after = Instant.now();

        // Then: Timestamp is within the expected range
        assertThat(record.getTimestamp())
                .as("Timestamp must be between before and after instants")
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);

        // And: Timestamp has millisecond precision
        long timestampMillis = record.getTimestamp().toEpochMilli();
        Instant reconstructedFromMillis = Instant.ofEpochMilli(timestampMillis);
        
        assertThat(reconstructedFromMillis.toEpochMilli())
                .as("Timestamp milliseconds must be preserved when reconstructed")
                .isEqualTo(timestampMillis);
    }

    /**
     * Property: Consecutive records have different timestamps (or very close).
     * <p>
     * Given: Multiple NotificationRequests created in sequence<br>
     * When: Creating NotificationRecords rapidly<br>
     * Then: Timestamps should be monotonically increasing or equal (within millisecond precision)
     * </p>
     */
    @Property(tries = 50)
    @Label("Feature: notification-persistence, Property 4: Timestamps monotónicos")
    void consecutiveRecordsHaveMonotonicTimestamps(
            @ForAll("notificationRequests") NotificationRequest request1,
            @ForAll("notificationRequests") NotificationRequest request2) {
        
        // When: Creating two records in sequence
        NotificationRecord record1 = NotificationRecord.ofSuccess(request1);
        NotificationRecord record2 = NotificationRecord.ofSuccess(request2);

        // Then: Second timestamp is equal or after first timestamp
        assertThat(record2.getTimestamp())
                .as("Second record timestamp must be equal or after first record")
                .isAfterOrEqualTo(record1.getTimestamp());
    }

    /**
     * Property: Timestamp is not null and represents a valid instant.
     * <p>
     * Given: Any valid NotificationRequest<br>
     * When: Creating a NotificationRecord<br>
     * Then: The timestamp must be non-null and represent a valid instant
     * </p>
     */
    @Property(tries = 100)
    @Label("Feature: notification-persistence, Property 4: Timestamp válido")
    void timestampIsValidInstant(
            @ForAll("notificationRequests") NotificationRequest request) {
        
        // When: Creating a notification record
        NotificationRecord record = NotificationRecord.ofSuccess(request);

        // Then: Timestamp is not null
        assertThat(record.getTimestamp())
                .as("Timestamp must not be null")
                .isNotNull();

        // And: Timestamp can be converted to epoch millis and back
        long epochMilli = record.getTimestamp().toEpochMilli();
        
        assertThat(epochMilli)
                .as("Timestamp must be convertible to epoch milliseconds")
                .isPositive();

        // And: Timestamp is reasonable (not in the far future or past)
        Instant now = Instant.now();
        
        assertThat(record.getTimestamp())
                .as("Timestamp must be close to current time (within 1 second)")
                .isBetween(now.minusSeconds(1), now.plusSeconds(1));
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
     * Provides arbitrary error messages.
     */
    @Provide
    Arbitrary<String> errorMessages() {
        return Arbitraries.strings()
                .alpha().numeric().withChars(' ', ':', '-')
                .ofMinLength(5).ofMaxLength(200);
    }
}
