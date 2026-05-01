package com.notifier.service;

import com.notifier.exception.PersistenceException;
import com.notifier.model.NotificationRecord;
import com.notifier.model.NotificationStatus;
import com.notifier.model.NotificationType;
import com.notifier.port.PersistencePort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Service responsible for querying persisted {@link NotificationRecord} instances.
 * <p>
 * This service acts as an intermediary between the API layer and the
 * {@link PersistencePort}, providing filtering capabilities by type, status,
 * and date range. All infrastructure exceptions are wrapped in
 * {@link PersistenceException} with a descriptive message so that callers
 * receive a consistent, technology-agnostic error signal.
 * </p>
 * <p>
 * When a query yields no results, an empty list is returned rather than
 * throwing an exception.
 * </p>
 */
@Service
public class QueryService {

    private final PersistencePort persistencePort;

    /**
     * Constructs a {@code QueryService} with the given persistence port.
     * <p>
     * Constructor injection is used to ensure immutability and to facilitate
     * unit testing of the service layer.
     * </p>
     *
     * @param persistencePort the port used to query notification records; must not be {@code null}
     */
    public QueryService(PersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    /**
     * Retrieves all persisted {@link NotificationRecord} instances.
     * <p>
     * Returns an empty list when no records exist. Never returns {@code null}.
     * </p>
     *
     * @return a list of all notification records, or an empty list if none exist
     * @throws PersistenceException if the underlying storage layer throws an exception
     */
    public List<NotificationRecord> findAll() {
        try {
            List<NotificationRecord> result = persistencePort.findAll();
            return result != null ? result : Collections.emptyList();
        } catch (PersistenceException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenceException("Failed to retrieve all notification records", e);
        }
    }

    /**
     * Retrieves all {@link NotificationRecord} instances whose delivery channel matches
     * the given {@link NotificationType}.
     * <p>
     * Returns an empty list when no matching records exist. Never returns {@code null}.
     * </p>
     *
     * @param type the delivery channel to filter by; must not be {@code null}
     * @return a list of records with the specified type, or an empty list if none match
     * @throws PersistenceException if the underlying storage layer throws an exception
     */
    public List<NotificationRecord> findByType(NotificationType type) {
        try {
            List<NotificationRecord> result = persistencePort.findByType(type);
            return result != null ? result : Collections.emptyList();
        } catch (PersistenceException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenceException(
                    "Failed to retrieve notification records by type: " + type, e);
        }
    }

    /**
     * Retrieves all {@link NotificationRecord} instances whose delivery outcome matches
     * the given {@link NotificationStatus}.
     * <p>
     * Returns an empty list when no matching records exist. Never returns {@code null}.
     * </p>
     *
     * @param status the delivery outcome to filter by; must not be {@code null}
     * @return a list of records with the specified status, or an empty list if none match
     * @throws PersistenceException if the underlying storage layer throws an exception
     */
    public List<NotificationRecord> findByStatus(NotificationStatus status) {
        try {
            List<NotificationRecord> result = persistencePort.findByStatus(status);
            return result != null ? result : Collections.emptyList();
        } catch (PersistenceException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenceException(
                    "Failed to retrieve notification records by status: " + status, e);
        }
    }

    /**
     * Retrieves all {@link NotificationRecord} instances whose {@code timestamp} falls
     * within the closed interval {@code [from, to]}.
     * <p>
     * Both bounds are inclusive. Returns an empty list when no records fall within the
     * range. Never returns {@code null}.
     * </p>
     *
     * @param from the start of the time range (inclusive); must not be {@code null}
     * @param to   the end of the time range (inclusive); must not be {@code null}
     * @return a list of records whose timestamp is within {@code [from, to]}, or an empty list if none match
     * @throws PersistenceException if the underlying storage layer throws an exception
     */
    public List<NotificationRecord> findByDateRange(Instant from, Instant to) {
        try {
            List<NotificationRecord> result = persistencePort.findByTimestampBetween(from, to);
            return result != null ? result : Collections.emptyList();
        } catch (PersistenceException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenceException(
                    "Failed to retrieve notification records for date range [" + from + ", " + to + "]", e);
        }
    }
}
