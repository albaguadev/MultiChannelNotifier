package com.notifier.strategy;

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

}
