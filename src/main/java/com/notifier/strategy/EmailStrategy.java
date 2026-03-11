package com.notifier.strategy;

/**
 * Email implementation of the NotificationStrategy.
 * Handles the logic required to send messages via the Email provider.
 */
public class EmailStrategy implements NotificationStrategy {

   /**
    * Sends a message through the Email channel.
    * @param message The content to send.
    */
   @Override
   public void send(String message) {
      System.out.println("Sending Email: " + message);
   }
}
