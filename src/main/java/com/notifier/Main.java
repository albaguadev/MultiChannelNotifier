package main.java.com.notifier;

import main.java.com.notifier.context.NotificationContext;
import main.java.com.notifier.factory.NotificationStrategyFactory;
import main.java.com.notifier.model.NotificationType;
import main.java.com.notifier.strategy.EmailStrategy;
import main.java.com.notifier.strategy.SmsStrategy;

public class Main {
    public static void main(String[] args) {

        NotificationContext context = new NotificationContext();

        context.setStrategy(NotificationStrategyFactory.getStrategy(NotificationType.EMAIL));
        context.executeStrategy("aguadoalbert@gmail.com ha escrito esto");

        context.setStrategy(NotificationStrategyFactory.getStrategy(NotificationType.SMS));
        context.executeStrategy("1567 ha escrito esto");

    }
}