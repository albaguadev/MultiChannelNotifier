package main.java.com.notifier.strategy;

public class SmsStrategy implements NotificationStrategy{

    @Override
    public void send(String message) {
        System.out.println("Sending Sms: " + message);
    }
}
