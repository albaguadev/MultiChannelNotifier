package com.notifier.context;

import com.notifier.strategy.NotificationStrategy;

public class NotificationContext {
    private NotificationStrategy strategy;

    public void setStrategy(NotificationStrategy strategy) {
        this.strategy = strategy;
    }

    public void executeStrategy(String message) {
        strategy.send(message);
    }
}
