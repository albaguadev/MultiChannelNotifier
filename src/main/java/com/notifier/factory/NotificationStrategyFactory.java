package com.notifier.factory;

import com.notifier.model.NotificationType;
import com.notifier.strategy.EmailStrategy;
import com.notifier.strategy.NotificationStrategy;
import com.notifier.strategy.SmsStrategy;

public class NotificationStrategyFactory {

    public static NotificationStrategy getStrategy(NotificationType type) {

        return switch (type) {
            case EMAIL -> new EmailStrategy();

            case SMS -> new SmsStrategy();
        };
    }

}
