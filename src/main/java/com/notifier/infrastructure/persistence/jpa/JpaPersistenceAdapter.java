package com.notifier.infrastructure.persistence.jpa;

import com.notifier.exception.PersistenceException;
import com.notifier.model.NotificationRecord;
import com.notifier.model.NotificationStatus;
import com.notifier.model.NotificationType;
import com.notifier.port.PersistencePort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * JPA-based implementation of {@link PersistencePort}.
 * <p>
 * This adapter lives in the infrastructure layer and translates between the domain model
 * ({@link NotificationRecord}) and the persistence model ({@link NotificationRecordEntity}).
 * All database exceptions are wrapped in {@link PersistenceException} to maintain
 * technology-agnostic error handling at the domain layer.
 * </p>
 * <p>
 * This implementation is active when the {@code jpa} profile is enabled. For development
 * and testing, this typically means H2; for production, PostgreSQL.
 * </p>
 */
@Component
@Profile("!test")  // Active in all profiles except test (test uses mock)
public class JpaPersistenceAdapter implements PersistencePort {

    private final JpaNotificationRecordRepository repository;

    /**
     * Constructs a {@code JpaPersistenceAdapter} with the given JPA repository.
     *
     * @param repository the Spring Data JPA repository; must not be {@code null}
     */
    public JpaPersistenceAdapter(JpaNotificationRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public NotificationRecord save(NotificationRecord record) {
        try {
            NotificationRecordEntity entity = NotificationRecordEntity.fromDomain(record);
            NotificationRecordEntity saved = repository.save(entity);
            return saved.toDomain();
        } catch (Exception e) {
            throw new PersistenceException("Failed to save notification record with id: " + record.getId(), e);
        }
    }

    @Override
    public List<NotificationRecord> findAll() {
        try {
            return repository.findAllByOrderByTimestampDesc().stream()
                    .map(NotificationRecordEntity::toDomain)
                    .toList();
        } catch (Exception e) {
            throw new PersistenceException("Failed to retrieve all notification records", e);
        }
    }

    @Override
    public List<NotificationRecord> findByType(NotificationType type) {
        try {
            return repository.findByType(type).stream()
                    .map(NotificationRecordEntity::toDomain)
                    .toList();
        } catch (Exception e) {
            throw new PersistenceException("Failed to retrieve notification records by type: " + type, e);
        }
    }

    @Override
    public List<NotificationRecord> findByStatus(NotificationStatus status) {
        try {
            return repository.findByStatus(status).stream()
                    .map(NotificationRecordEntity::toDomain)
                    .toList();
        } catch (Exception e) {
            throw new PersistenceException("Failed to retrieve notification records by status: " + status, e);
        }
    }

    @Override
    public List<NotificationRecord> findByTimestampBetween(Instant from, Instant to) {
        try {
            return repository.findByTimestampBetween(from, to).stream()
                    .map(NotificationRecordEntity::toDomain)
                    .toList();
        } catch (Exception e) {
            throw new PersistenceException(
                    "Failed to retrieve notification records for date range [" + from + ", " + to + "]", e);
        }
    }
}
