package com.notifier;

import com.notifier.context.NotificationContext;
import com.notifier.factory.NotificationStrategyFactory;
import com.notifier.model.NotificationType;

/**
 * Entry point for the Notification System application.
 * <p>
 * This class demonstrates the end-to-end flow of the <b>Strategy</b> and
 * <b>Factory</b> patterns:
 * <ol>
 * <li>Selecting a {@link NotificationType}.</li>
 * <li>Obtaining a strategy from {@link NotificationStrategyFactory}.</li>
 * <li>Configuring the {@link NotificationContext}.</li>
 * <li>Executing the delivery logic.</li>
 * </ol>
 * </p>
 */
public class Main {

    public static void main(String[] args) {

        NotificationContext context = new NotificationContext();

        context.setStrategy(NotificationStrategyFactory.getStrategy(NotificationType.EMAIL));
        context.executeStrategy("fake@gmail.com ha escrito esto");

        context.setStrategy(NotificationStrategyFactory.getStrategy(NotificationType.SMS));
        context.executeStrategy("1567 ha escrito esto");

        context.setStrategy(NotificationStrategyFactory.getStrategy(NotificationType.WHATSAPP));
        context.executeStrategy("telefonillo ha escrito esto");

    }
}