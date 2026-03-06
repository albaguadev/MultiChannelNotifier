package com.notifier.strategy;

public class WhatsAppStrategy implements NotificationStrategy{
    @Override
    public void send(String message) {
        System.out.println("Sending WhatsApp: " + message);
    }
}
