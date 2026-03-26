package com.notifier.strategy;

import com.notifier.model.NotificationType;
import org.springframework.stereotype.Component;

/**
 * Email implementation of the NotificationStrategy.
 * Handles the logic required to send messages via the Email provider.
 */
@Component
public class EmailStrategy implements NotificationStrategy {

   /**
    * Sends a message through the Email channel.
    * @param message The content to send.
    */
   @Override
   public void send(String message) {
      System.out.println("Sending Email: " + message);
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
}
