package com.notifier.strategy;

import com.notifier.model.NotificationType;

/**
 * Common interface for all notification delivery algorithms.
 * This interface defines the contract that every specific provider
 * (Email, SMS, WhatsApp) must implement.
 * * Part of the Strategy Design Pattern.
 */
public interface NotificationStrategy {

    /**
     * Executes the sending of a message.
     * @param message The text content to be delivered.
     */
    void send(String message);

    /**
     * The specific {@link NotificationType} associated with the implementation is returned.
     * This metadata is utilized by the factory for automated strategy resolution.
     * * @return The unique identifier for the notification channel.
     */
    NotificationType getType();
}
