package main.java.com.notifier.strategy;

public class EmailStrategy implements NotificationStrategy {

   @Override
   public void send(String message) {
      System.out.println("Sending Email: " + message);
   }
}
