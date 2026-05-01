package com.notifier.infrastructure.persistence.jpa;

import com.notifier.model.NotificationStatus;
import com.notifier.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data JPA repository for {@link NotificationRecordEntity}.
 * <p>
 * This interface provides CRUD operations and custom query methods derived from
 * method names. Spring Data JPA generates the implementation automatically at runtime.
 * </p>
 */
@Repository
public interface JpaNotificationRecordRepository extends JpaRepository<NotificationRecordEntity, String> {

    /**
     * Finds all notification records with the specified delivery channel.
     *
     * @param type the delivery channel to filter by; must not be {@code null}
     * @return a list of entities with the specified type, or an empty list if none match
     */
    List<NotificationRecordEntity> findByType(NotificationType type);

    /**
     * Finds all notification records with the specified delivery outcome.
     *
     * @param status the delivery outcome to filter by; must not be {@code null}
     * @return a list of entities with the specified status, or an empty list if none match
     */
    List<NotificationRecordEntity> findByStatus(NotificationStatus status);

    /**
     * Finds all notification records whose timestamp falls within the closed interval {@code [from, to]}.
     *
     * @param from the start of the time range (inclusive); must not be {@code null}
     * @param to   the end of the time range (inclusive); must not be {@code null}
     * @return a list of entities whose timestamp is within {@code [from, to]}, or an empty list if none match
     */
    List<NotificationRecordEntity> findByTimestampBetween(Instant from, Instant to);

    /**
     * Finds all notification records, ordered by timestamp in descending order (most recent first).
     *
     * @return a list of all entities ordered by timestamp descending, or an empty list if none exist
     */
    List<NotificationRecordEntity> findAllByOrderByTimestampDesc();
}
