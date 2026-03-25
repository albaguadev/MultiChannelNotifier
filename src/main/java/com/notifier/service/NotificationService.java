package com.notifier.service;

import com.notifier.dto.NotificationRequest;
import com.notifier.factory.NotificationStrategyFactory;
import com.notifier.strategy.NotificationStrategy;
import org.springframework.stereotype.Service;

/**
 * Business logic orchestration for the notification system is handled by this service.
 * It acts as an intermediary between the API controller and the strategy factory.
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
     *
     * @param request The validated notification payload containing type and content.
     */
    public void sendNotification(NotificationRequest request) {
        NotificationStrategy strategy = factory.getStrategy(request.getType());
        strategy.send(request.getMessage());
    }
}