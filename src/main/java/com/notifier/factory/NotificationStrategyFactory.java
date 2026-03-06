package main.java.com.notifier.factory;

import main.java.com.notifier.model.NotificationType;
import main.java.com.notifier.strategy.EmailStrategy;
import main.java.com.notifier.strategy.NotificationStrategy;
import main.java.com.notifier.strategy.SmsStrategy;

public class NotificationStrategyFactory {

    public static NotificationStrategy getStrategy(NotificationType type) {

        return switch (type) {
            case EMAIL -> new EmailStrategy();

            case SMS -> new SmsStrategy();
        };
    }

}
