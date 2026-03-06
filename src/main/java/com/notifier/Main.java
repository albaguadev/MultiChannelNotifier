package com.notifier;

import com.notifier.context.NotificationContext;
import com.notifier.factory.NotificationStrategyFactory;
import com.notifier.model.NotificationType;

public class Main {
    public static void main(String[] args) {

        NotificationContext context = new NotificationContext();

        context.setStrategy(NotificationStrategyFactory.getStrategy(NotificationType.EMAIL));
        context.executeStrategy("aguadoalbert@gmail.com ha escrito esto");

        context.setStrategy(NotificationStrategyFactory.getStrategy(NotificationType.SMS));
        context.executeStrategy("1567 ha escrito esto");

    }
}