package com.notifier.strategy;

/**
 * WhatsApp implementation of the NotificationStrategy.
 * Handles the logic required to send messages via the WhatsApp provider.
 */
public class WhatsAppStrategy implements NotificationStrategy{

    /**
     * Sends a message through the WhatsApp channel.
     * @param message The content to send.
     */
    @Override
    public void send(String message) {
        System.out.println("Sending WhatsApp: " + message);
    }
}
