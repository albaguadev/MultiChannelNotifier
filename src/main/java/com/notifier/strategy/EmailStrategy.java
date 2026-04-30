package com.notifier.strategy;

import com.notifier.dto.NotificationRequest;
import com.notifier.exception.InvalidNotificationException;
import com.notifier.model.NotificationType;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.springframework.stereotype.Component;

/**
 * Email implementation of the NotificationStrategy.
 * Handles the logic required to send messages via the Email provider.
 */
@Component
public class EmailStrategy implements NotificationStrategy {

   /**
    * Sends a message through the Email channel.
    * <p>
    * Note: this method internally calls {@link #validate(NotificationRequest)} before
    * dispatching. When invoked through the standard service flow, validation will
    * have already been performed by {@code NotificationService}; the internal call
    * here acts as a safeguard for direct strategy usage.
    * </p>
    * @param request The content to send.
    */
   @Override
   public void send(NotificationRequest request) {

      validate(request);

      System.out.println("Executing Email Delivery to: " + request.getRecipient());
      System.out.println("Subject: " + request.getSubject());
      System.out.println("Body: " + request.getMessage());
   }

   /**
    * The unique identifier for the Email notification type is returned.
    * This metadata is processed by the strategy factory during the
    * dependency injection lifecycle.
    * * @return The {@link NotificationType#EMAIL} constant.
    */
   @Override
   public NotificationType getType() {
      return NotificationType.EMAIL;
   }

   @Override
   public void validate(NotificationRequest request) {
      EmailValidator validator = new EmailValidator();
      if (!validator.isValid(request.getRecipient(), null)) {
         throw new InvalidNotificationException("Invalid Email format: " + request.getRecipient());
      }
   }
}
