package com.notifier.dto;

import com.notifier.model.NotificationRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Data Transfer Object that represents the outbound view of a persisted notification record.
 * <p>
 * All enum values ({@code type}, {@code status}) are serialised as their string names so that
 * API consumers are not coupled to the internal enum types.  The {@code timestamp} field is
 * expressed as an ISO-8601 string, which is the default format produced by
 * {@link java.time.Instant#toString()}.
 * </p>
 *
 * <p>Instances are created exclusively through the static factory method
 * {@link #from(NotificationRecord)}, which maps every field from the domain record.</p>
 */
@Getter
@AllArgsConstructor
public class NotificationRecordResponse {

    /**
     * Unique identifier of the notification record.
     */
    private final String id;

    /**
     * Delivery channel used for the notification (e.g. {@code "EMAIL"}, {@code "SMS"}).
     */
    private final String type;

    /**
     * Recipient address or identifier (e.g. email address, phone number).
     */
    private final String recipient;

    /**
     * Main content of the notification.
     */
    private final String message;

    /**
     * Optional subject line; {@code null} for non-email channels.
     */
    private final String subject;

    /**
     * Delivery outcome (e.g. {@code "SENT"}, {@code "FAILED"}).
     */
    private final String status;

    /**
     * Human-readable error description when the delivery failed; otherwise {@code null}.
     */
    private final String errorMessage;

    /**
     * ISO-8601 timestamp indicating when the record was created (e.g. {@code "2024-06-01T12:00:00Z"}).
     */
    private final String timestamp;

    /**
     * Creates a {@code NotificationRecordResponse} from the given domain record.
     * <p>
     * Enum fields are converted to their {@link Enum#name()} string representation, and the
     * {@link java.time.Instant} timestamp is converted via {@link java.time.Instant#toString()},
     * which produces an ISO-8601 string with millisecond precision.
     * </p>
     *
     * @param record the domain record to map; must not be {@code null}
     * @return a new {@code NotificationRecordResponse} populated from {@code record}
     */
    public static NotificationRecordResponse from(NotificationRecord record) {
        return new NotificationRecordResponse(
                record.getId(),
                record.getType().name(),
                record.getRecipient(),
                record.getMessage(),
                record.getSubject(),
                record.getStatus().name(),
                record.getErrorMessage(),
                record.getTimestamp().toString()
        );
    }
}
