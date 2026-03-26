package com.notifier.factory;

import com.notifier.model.NotificationType;
import com.notifier.strategy.NotificationStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Strategy resolution is managed dynamically through this factory component.
 * All beans implementing the {@link NotificationStrategy} interface are
 * automatically discovered and registered during application startup.
 */
@Component
public class NotificationStrategyFactory {

    private final Map<NotificationType, NotificationStrategy> strategyMap;

    /**
     * The strategy collection is injected via constructor and processed into a map.
     * This mapping ensures O(1) retrieval performance based on the notification type.
     * * @param strategies A collection of all managed beans implementing {@link NotificationStrategy}.
     */
    public NotificationStrategyFactory(List<NotificationStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(NotificationStrategy::getType, Function.identity()));
    }

    /**
     * The appropriate delivery strategy is retrieved from the pre-populated map.
     * * @param type The required notification type for message delivery.
     * @return The specific strategy implementation corresponding to the type.
     * @throws IllegalArgumentException if no implementation is found for the requested type.
     */
    public NotificationStrategy getStrategy(NotificationType type) {
        return Optional.ofNullable(strategyMap.get(type))
                .orElseThrow(() -> new IllegalArgumentException("No strategy implementation was found for: " + type));
    }
}