package com.notifier.infrastructure.persistence.jpa;

import com.notifier.model.NotificationRecord;
import com.notifier.model.NotificationStatus;
import com.notifier.model.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * JPA entity that maps {@link NotificationRecord} to a relational database table.
 * <p>
 * This entity lives in the infrastructure layer and is kept strictly separate from
 * the domain model. Conversion between {@code NotificationRecord} (domain) and
 * {@code NotificationRecordEntity} (persistence) is handled by the repository adapter.
 * </p>
 */
@Entity
@Table(name = "notification_records",
       indexes = {
           @Index(name = "idx_notification_type", columnList = "type"),
           @Index(name = "idx_notification_status", columnList = "status"),
           @Index(name = "idx_notification_timestamp", columnList = "timestamp")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRecordEntity {

    /**
     * Unique identifier for this record.
     * <p>
     * Stored as a string (UUID format) rather than a native UUID type for maximum
     * portability across H2, PostgreSQL, and other databases.
     * </p>
     */
    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    /**
     * Delivery channel used for the notification.
     * <p>
     * Stored as a string via {@link Enumerated(EnumType.STRING)} to ensure
     * database-level readability and avoid ordinal-based fragility.
     * </p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationType type;

    /**
     * Recipient address or identifier (e.g. email address, phone number).
     */
    @Column(name = "recipient", nullable = false, length = 500)
    private String recipient;

    /**
     * Main content of the notification.
     * <p>
     * Mapped to {@code TEXT} to accommodate messages of arbitrary length.
     * </p>
     */
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Optional subject line; {@code null} for non-email channels.
     */
    @Column(name = "subject", length = 500)
    private String subject;

    /**
     * Delivery outcome.
     * <p>
     * Stored as a string via {@link Enumerated(EnumType.STRING)}.
     * </p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private NotificationStatus status;

    /**
     * Human-readable error description when the delivery failed; otherwise {@code null}.
     * <p>
     * Mapped to {@code TEXT} to accommodate error messages of arbitrary length.
     * </p>
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Timestamp indicating when the record was created, with millisecond precision.
     * <p>
     * Stored as {@link Instant} which JPA 3.x (Jakarta Persistence) maps natively
     * to {@code TIMESTAMP} with timezone support.
     * </p>
     */
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    /**
     * Converts this JPA entity to a domain {@link NotificationRecord}.
     *
     * @return a new {@code NotificationRecord} populated from this entity
     */
    public NotificationRecord toDomain() {
        return new NotificationRecord(id, type, recipient, message, subject, status, errorMessage, timestamp);
    }

    /**
     * Creates a JPA entity from a domain {@link NotificationRecord}.
     *
     * @param record the domain record to convert; must not be {@code null}
     * @return a new {@code NotificationRecordEntity} populated from {@code record}
     */
    public static NotificationRecordEntity fromDomain(NotificationRecord record) {
        return new NotificationRecordEntity(
                record.getId(),
                record.getType(),
                record.getRecipient(),
                record.getMessage(),
                record.getSubject(),
                record.getStatus(),
                record.getErrorMessage(),
                record.getTimestamp()
        );
    }
}
