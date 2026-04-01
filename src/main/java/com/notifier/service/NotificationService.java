package com.notifier.service;

import com.notifier.dto.NotificationRequest;
import com.notifier.factory.NotificationStrategyFactory;
import com.notifier.strategy.NotificationStrategy;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Business logic orchestration for the notification system is handled by this service.
 * It acts as an intermediary between the API controller and the strategy factory,
 * ensuring that the message delivery is correctly delegated.
 */
@Service
public class NotificationService {

    private final NotificationStrategyFactory factory;

    /**
     * The strategy factory is injected via constructor to ensure immutability
     * and facilitate unit testing of the service layer.
     *
     * @param factory The managed bean responsible for strategy resolution.
     */
    public NotificationService(NotificationStrategyFactory factory) {
        this.factory = factory;
    }

    /**
     * The dispatching process is coordinated by retrieving the appropriate
     * strategy and executing the delivery logic.
     * Pre-execution checks are performed to ensure data integrity.
     *
     * @param request The validated notification payload containing type and content.
     * @throws IllegalArgumentException if the message content is null or blank.
     */
    public void sendNotification(NotificationRequest request) {
        Assert.notNull(request.getMessage(), "Message error: Notification content cannot be null.");
        Assert.hasText(request.getMessage(), "Message error: Cannot send an empty notification.");

        NotificationStrategy strategy = factory.getStrategy(request.getType());

        strategy.validate(request);
        strategy.send(request);
    }
}