package com.notifier.factory;

import com.notifier.model.NotificationType;
import com.notifier.strategy.EmailStrategy;
import com.notifier.strategy.NotificationStrategy;
import com.notifier.strategy.SmsStrategy;
import com.notifier.strategy.WhatsAppStrategy;

/**
 * Main factory class for creating {@link NotificationStrategy} instances.
 * <p>
 * This class eliminates the need for the client to manually instantiate
 * specific strategies like {@link EmailStrategy} or {@link SmsStrategy}.
 * </p>
 */
public class NotificationStrategyFactory {

    /**
     * Factory method that returns a concrete strategy based on the type.
     * * @param type The {@link NotificationType} requested by the user.
     * @return A concrete instance of {@link NotificationStrategy}.
     * @throws IllegalArgumentException if the provided type is null or unsupported.
     */
    public static NotificationStrategy getStrategy(NotificationType type) {

        return switch (type) {
            case EMAIL -> new EmailStrategy();

            case SMS -> new SmsStrategy();

            case WHATSAPP -> new WhatsAppStrategy();
        };
    }

}
