package com.notifier.strategy;

import com.notifier.model.NotificationType;
import org.springframework.stereotype.Component;

/**
 * WhatsApp implementation of the NotificationStrategy.
 * Handles the logic required to send messages via the WhatsApp provider.
 */
@Component
public class WhatsAppStrategy implements NotificationStrategy{

    /**
     * Sends a message through the WhatsApp channel.
     * @param message The content to send.
     */
    @Override
    public void send(String message) {
        System.out.println("Sending WhatsApp: " + message);
    }

    /**
     * The unique identifier for the WhatsApp notification type is returned.
     * This metadata is processed by the strategy factory during the
     * dependency injection lifecycle.
     * * @return The {@link NotificationType#WHATSAPP} constant.
     */
    @Override
    public NotificationType getType() {
        return NotificationType.WHATSAPP;
    }
}
