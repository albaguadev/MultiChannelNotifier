package com.notifier.model;

import com.notifier.dto.NotificationRequest;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable domain record that captures the outcome of a single notification delivery attempt.
 * <p>
 * Instances are created exclusively through the static factory methods
 * {@link #ofSuccess(NotificationRequest)} and {@link #ofFailure(NotificationRequest, String)},
 * which guarantee that every record has a unique identifier and a precise timestamp.
 * </p>
 * <p>
 * The class intentionally exposes only getters (via Lombok {@code @Getter}) to preserve
 * immutability after construction.
 * </p>
 */
@Getter
public class NotificationRecord {

    /** Unique identifier for this record, generated as a random UUID string. */
    private final String id;

    /** The delivery channel used for this notification. */
    private final NotificationType type;

    /** The recipient address or identifier (e.g. email address, phone number). */
    private final String recipient;

    /** The main content of the notification. */
    private final String message;

    /** Optional subject line; may be {@code null} for non-email channels. */
    private final String subject;

    /** The outcome of the delivery attempt. */
    private final NotificationStatus status;

    /** Human-readable error description when {@code status} is {@link NotificationStatus#FAILED}; otherwise {@code null}. */
    private final String errorMessage;

    /** The instant at which this record was created, with millisecond precision. */
    private final Instant timestamp;

    /**
     * All-args constructor used by factory methods and infrastructure adapters.
     * <p>
     * This constructor is public to allow infrastructure adapters (e.g. JPA, serialization
     * frameworks) to reconstitute domain objects from persistent storage. However, application
     * code should prefer the factory methods {@link #ofSuccess} and {@link #ofFailure} for
     * creating new instances, as they enforce domain invariants and generate proper identifiers.
     * </p>
     *
     * @param id           unique record identifier
     * @param type         notification delivery channel
     * @param recipient    recipient address or identifier
     * @param message      notification body
     * @param subject      optional subject line (may be {@code null})
     * @param status       delivery outcome
     * @param errorMessage error description (may be {@code null})
     * @param timestamp    creation instant
     */
    public NotificationRecord(String id,
                                NotificationType type,
                                String recipient,
                                String message,
                                String subject,
                                NotificationStatus status,
                                String errorMessage,
                                Instant timestamp) {
        this.id = id;
        this.type = type;
        this.recipient = recipient;
        this.message = message;
        this.subject = subject;
        this.status = status;
        this.errorMessage = errorMessage;
        this.timestamp = timestamp;
    }

    /**
     * Creates a {@code NotificationRecord} representing a successful delivery.
     * <p>
     * The record is assigned a randomly generated UUID, a status of
     * {@link NotificationStatus#SENT}, a {@code null} error message, and a
     * timestamp equal to {@link Instant#now()} at the moment of invocation.
     * </p>
     *
     * @param request the original notification request; must not be {@code null}
     * @return a new {@code NotificationRecord} with {@code status == SENT}
     */
    public static NotificationRecord ofSuccess(NotificationRequest request) {
        return new NotificationRecord(
                UUID.randomUUID().toString(),
                request.getType(),
                request.getRecipient(),
                request.getMessage(),
                request.getSubject(),
                NotificationStatus.SENT,
                null,
                Instant.now()
        );
    }

    /**
     * Creates a {@code NotificationRecord} representing a failed delivery attempt.
     * <p>
     * The record is assigned a randomly generated UUID, a status of
     * {@link NotificationStatus#FAILED}, the provided error message, and a
     * timestamp equal to {@link Instant#now()} at the moment of invocation.
     * </p>
     * <p>
     * <strong>Note:</strong> While this method accepts {@code null} for {@code errorMessage}
     * to maintain flexibility, callers should provide a meaningful error description whenever
     * possible. {@link NotificationService} guarantees a non-null message by using a default
     * when the exception message is unavailable.
     * </p>
     *
     * @param request      the original notification request; must not be {@code null}
     * @param errorMessage a human-readable description of the failure cause; may be {@code null}
     * @return a new {@code NotificationRecord} with {@code status == FAILED}
     */
    public static NotificationRecord ofFailure(NotificationRequest request, String errorMessage) {
        return new NotificationRecord(
                UUID.randomUUID().toString(),
                request.getType(),
                request.getRecipient(),
                request.getMessage(),
                request.getSubject(),
                NotificationStatus.FAILED,
                errorMessage,
                Instant.now()
        );
    }
}
