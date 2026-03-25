package com.notifier.dto;

import com.notifier.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * The incoming notification payload is represented by this Data Transfer Object.
 * Validation constraints are applied to ensure data integrity before processing.
 */
@Data
public class NotificationRequest {

    /**
     * The delivery channel is specified via this field.
     * A null value is not permitted.
     */
    @NotNull(message = "Notification type is required")
    private NotificationType type;

    /**
     * The intended recipient's address or identifier is stored here.
     * Empty strings or whitespace are rejected by validation.
     */
    @NotBlank(message = "Recipient must not be blank")
    private String recipient;

    /**
     * The main content of the notification is contained within this field.
     * Mandatory for all notification types.
     */
    @NotBlank(message = "Message content must not be blank")
    private String message;

    /**
     * An optional field provided for titled content.
     * It is primarily utilized by the Email strategy as the subject line.
     */
    private String subject;
}