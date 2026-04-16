package com.notifier.strategy;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.notifier.dto.NotificationRequest;
import com.notifier.exception.InvalidNotificationException;
import com.notifier.model.NotificationType;
import org.springframework.stereotype.Component;

/**
 * WhatsApp implementation of the NotificationStrategy.
 * Handles the logic required to send messages via the WhatsApp provider.
 */
@Component
public class WhatsAppStrategy implements NotificationStrategy{

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    /**
     * Sends a message through the WhatsApp channel.
     * @param request The content to send.
     */
    @Override
    public void send(NotificationRequest request) {
        validate(request);
        System.out.println("Sending WhatsApp Message to " + request.getRecipient());
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

    @Override
    public void validate(NotificationRequest request) {
        try {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(request.getRecipient(), null);

            if (!phoneUtil.isValidNumber(numberProto)) {
                throw new InvalidNotificationException("Invalid WhatsApp number: " + request.getRecipient());
            }

            if (!request.getRecipient().startsWith("+")) {
                throw new InvalidNotificationException("WhatsApp requires E.164 format (starting with +)");
            }
        } catch (NumberParseException e) {
            throw new InvalidNotificationException("Malformed WhatsApp identifier: " + request.getRecipient());
        }
    }
}
