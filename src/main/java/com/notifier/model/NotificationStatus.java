package com.notifier.model;

/**
 * Represents the outcome of a notification delivery attempt.
 * <p>
 * This status is recorded alongside each {@code NotificationRecord}
 * to indicate whether the notification was delivered successfully
 * or whether the delivery failed.
 * </p>
 */
public enum NotificationStatus {

    /** The notification was delivered successfully. */
    SENT,

    /** The notification could not be delivered due to an error. */
    FAILED
}
