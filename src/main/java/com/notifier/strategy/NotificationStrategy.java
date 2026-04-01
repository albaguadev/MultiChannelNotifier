package com.notifier.strategy;

import com.notifier.dto.NotificationRequest;
import com.notifier.exception.InvalidNotificationException;
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
     * @param request The data to be delivered.
     */
    void send(NotificationRequest request);

    /**
     * The specific {@link NotificationType} associated with the implementation is returned.
     * This metadata is utilized by the factory for automated strategy resolution.
     * * @return The unique identifier for the notification channel.
     */
    NotificationType getType();

    /**
     * Validates the request according to the specific channel rules.
     * @param request The full notification data.
     * @throws InvalidNotificationException if the format is incorrect (e.g., invalid Email/SMS).
     */
    void validate(NotificationRequest request);
}
