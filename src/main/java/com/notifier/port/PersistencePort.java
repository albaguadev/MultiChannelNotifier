package com.notifier.port;

import com.notifier.model.NotificationRecord;
import com.notifier.model.NotificationStatus;
import com.notifier.model.NotificationType;

import java.time.Instant;
import java.util.List;

/**
 * Technology-agnostic port for persisting and querying {@link NotificationRecord} instances.
 * <p>
 * This interface belongs to the domain layer and deliberately avoids any reference to a
 * concrete storage technology (JPA, MongoDB, SQL, etc.). Adapters that implement this
 * interface live in the infrastructure layer and are responsible for translating between
 * the domain model and the underlying data store.
 * </p>
 * <p>
 * All methods may throw unchecked exceptions (e.g. {@code PersistenceException}) when the
 * underlying store is unavailable or returns an unexpected error; callers should handle
 * those cases appropriately.
 * </p>
 */
public interface PersistencePort {

    /**
     * Persists a {@link NotificationRecord} and returns the stored instance.
     * <p>
     * Implementations must guarantee that the returned record is equivalent to the one
     * passed in (same {@code id}, {@code type}, {@code recipient}, {@code message},
     * {@code subject}, {@code status}, {@code errorMessage} and {@code timestamp}).
     * </p>
     *
     * @param record the record to persist; must not be {@code null}
     * @return the persisted record, never {@code null}
     */
    NotificationRecord save(NotificationRecord record);

    /**
     * Retrieves all persisted {@link NotificationRecord} instances.
     * <p>
     * Returns an empty list when no records exist; never returns {@code null}.
     * </p>
     *
     * @return an unmodifiable list of all records, ordered by implementation-defined criteria
     */
    List<NotificationRecord> findAll();

    /**
     * Retrieves all {@link NotificationRecord} instances whose delivery channel matches
     * the given {@link NotificationType}.
     * <p>
     * Returns an empty list when no matching records exist; never returns {@code null}.
     * </p>
     *
     * @param type the delivery channel to filter by; must not be {@code null}
     * @return an unmodifiable list of records with the specified type
     */
    List<NotificationRecord> findByType(NotificationType type);

    /**
     * Retrieves all {@link NotificationRecord} instances whose delivery outcome matches
     * the given {@link NotificationStatus}.
     * <p>
     * Returns an empty list when no matching records exist; never returns {@code null}.
     * </p>
     *
     * @param status the delivery outcome to filter by; must not be {@code null}
     * @return an unmodifiable list of records with the specified status
     */
    List<NotificationRecord> findByStatus(NotificationStatus status);

    /**
     * Retrieves all {@link NotificationRecord} instances whose {@code timestamp} falls
     * within the closed interval {@code [from, to]}.
     * <p>
     * Both bounds are inclusive. Returns an empty list when no records fall within the
     * range; never returns {@code null}.
     * </p>
     *
     * @param from the start of the time range (inclusive); must not be {@code null}
     * @param to   the end of the time range (inclusive); must not be {@code null} and
     *             must not be before {@code from}
     * @return an unmodifiable list of records whose timestamp is within {@code [from, to]}
     */
    List<NotificationRecord> findByTimestampBetween(Instant from, Instant to);
}
