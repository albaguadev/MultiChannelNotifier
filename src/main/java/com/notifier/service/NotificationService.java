package com.notifier.service;

import com.notifier.dto.NotificationRequest;
import com.notifier.factory.NotificationStrategyFactory;
import com.notifier.model.NotificationRecord;
import com.notifier.port.PersistencePort;
import com.notifier.strategy.NotificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Business logic orchestration for the notification system is handled by this service.
 * It acts as an intermediary between the API controller and the strategy factory,
 * ensuring that the message delivery is correctly delegated.
 * <p>
 * Every delivery attempt — whether successful or failed — is persisted via
 * {@link PersistencePort}. Persistence failures are logged but never propagated
 * to the caller, so that a storage outage does not prevent notifications from
 * being sent.
 * </p>
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationStrategyFactory factory;
    private final PersistencePort persistencePort;

    /**
     * The strategy factory and persistence port are injected via constructor to ensure
     * immutability and facilitate unit testing of the service layer.
     *
     * @param factory         The managed bean responsible for strategy resolution.
     * @param persistencePort The port used to persist notification records.
     */
    public NotificationService(NotificationStrategyFactory factory, PersistencePort persistencePort) {
        this.factory = factory;
        this.persistencePort = persistencePort;
    }

    /**
     * The dispatching process is coordinated by retrieving the appropriate
     * strategy and executing the delivery logic.
     * Pre-execution checks are performed to ensure data integrity.
     * <p>
     * A {@link NotificationRecord} is always persisted after the attempt:
     * {@link NotificationRecord#ofSuccess(NotificationRequest)} on success, or
     * {@link NotificationRecord#ofFailure(NotificationRequest, String)} on failure.
     * Persistence errors are swallowed and logged so that they do not mask the
     * original delivery exception.
     * </p>
     *
     * @param request The validated notification payload containing type and content.
     * @throws IllegalArgumentException if the message content is null or blank.
     * @throws RuntimeException         if the underlying strategy throws during validation or sending.
     */
    public void sendNotification(NotificationRequest request) {
        Assert.notNull(request.getMessage(), "Message error: Notification content cannot be null.");
        Assert.hasText(request.getMessage(), "Message error: Cannot send an empty notification.");

        NotificationStrategy strategy = factory.getStrategy(request.getType());

        NotificationRecord record = null;
        try {
            strategy.validate(request);
            strategy.send(request);
            record = NotificationRecord.ofSuccess(request);
        } catch (RuntimeException e) {
            String errorMessage = e.getMessage() != null 
                ? e.getMessage() 
                : "Unknown error: " + e.getClass().getSimpleName();
            record = NotificationRecord.ofFailure(request, errorMessage);
            throw e;
        } finally {
            if (record != null) {
                persistSafely(record);
            }
        }
    }

    /**
     * Persists the given {@link NotificationRecord} without propagating any exception.
     * <p>
     * If {@link PersistencePort#save(NotificationRecord)} throws, the error is logged
     * at {@code ERROR} level and the exception is swallowed so that the caller's flow
     * is not interrupted.
     * </p>
     *
     * @param record the record to persist; must not be {@code null}
     */
    private void persistSafely(NotificationRecord record) {
        try {
            persistencePort.save(record);
        } catch (Exception e) {
            log.error("Failed to persist notification record: {}", e.getMessage(), e);
        }
    }
}
