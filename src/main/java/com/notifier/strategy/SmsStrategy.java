package com.notifier.strategy;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.notifier.dto.NotificationRequest;
import com.notifier.exception.InvalidNotificationException;
import com.notifier.model.NotificationType;
import org.springframework.stereotype.Component;

/**
 * SMS implementation of the NotificationStrategy.
 * Handles the logic required to send messages via the SMS provider.
 */
@Component
public class SmsStrategy implements NotificationStrategy{

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();


    /**
     * Sends a message through the SMS channel.
     * @param request The content to send.
     */
    @Override
    public void send(NotificationRequest request) {
        validate(request);
        System.out.println("Sending SMS to " + request.getRecipient());
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

    @Override
    public void validate(NotificationRequest request) {
        try {

            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(request.getRecipient(), "ES");

            if (!phoneUtil.isValidNumber(numberProto)) {
                throw new InvalidNotificationException("The phone number is not valid: " + request.getRecipient());
            }
        } catch (NumberParseException e) {
            throw new InvalidNotificationException("Impossible to parse phone number: " + request.getRecipient());
        }
    }
}
