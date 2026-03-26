package com.notifier.strategy;

import com.notifier.model.NotificationType;
import org.springframework.stereotype.Component;

/**
 * SMS implementation of the NotificationStrategy.
 * Handles the logic required to send messages via the SMS provider.
 */
@Component
public class SmsStrategy implements NotificationStrategy{

    /**
     * Sends a message through the SMS channel.
     * @param message The content to send.
     */
    @Override
    public void send(String message) {
        System.out.println("Sending Sms: " + message);
    }

    /**
     * The unique identifier for the Sms notification type is returned.
     * This metadata is processed by the strategy factory during the
     * dependency injection lifecycle.
     * * @return The {@link NotificationType#SMS} constant.
     */
    @Override
    public NotificationType getType() {
        return NotificationType.SMS;
    }
}
