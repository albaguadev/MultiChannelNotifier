package com.notifier.strategy;

/**
 * SMS implementation of the NotificationStrategy.
 * Handles the logic required to send messages via the SMS provider.
 */
public class SmsStrategy implements NotificationStrategy{

    /**
     * Sends a message through the SMS channel.
     * @param message The content to send.
     */
    @Override
    public void send(String message) {
        System.out.println("Sending Sms: " + message);
    }
}
