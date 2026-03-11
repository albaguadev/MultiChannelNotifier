package com.notifier.model;

/**
 * Supported delivery channels for the notification system.
 * <p>
 * These constants are used by the factory to determine which
 * concrete strategy should be instantiated.
 * </p>
 */
public enum NotificationType {
    EMAIL,
    SMS,
    WHATSAPP
}
