package com.notifier.context;

import com.notifier.strategy.NotificationStrategy;

/**
 * The context in which notifications are executed.
 * <p>
 * This class maintains a reference to a {@link NotificationStrategy} object
 * and delegates the sending task to it. It allows switching strategies
 * dynamically at runtime.
 * </p>
 */
public class NotificationContext {
    private NotificationStrategy strategy;

    /**
     * Configures the active strategy for this context.
     * @param strategy The {@link NotificationStrategy} implementation to be used.
     */
    public void setStrategy(NotificationStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Executes the delivery logic using the currently set strategy.
     * * @param message The text content to be sent.
     * @throws IllegalStateException if no strategy has been set via {@link #setStrategy(NotificationStrategy)}.
     * @throws IllegalArgumentException if the message is null or empty.
     */
    public void executeStrategy(String message) {
        if (this.strategy == null) {
            throw new IllegalStateException("Context error: No notification strategy has been configured.");
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message error: Cannot send a null or empty notification.");
        }

        strategy.send(message);
    }
}