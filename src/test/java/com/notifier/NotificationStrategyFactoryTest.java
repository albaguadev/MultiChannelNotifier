package com.notifier;

import com.notifier.factory.NotificationStrategyFactory;
import com.notifier.model.NotificationType;
import com.notifier.strategy.EmailStrategy;
import com.notifier.strategy.NotificationStrategy;
import com.notifier.strategy.SmsStrategy;
import com.notifier.strategy.WhatsAppStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NotificationStrategyFactoryTest {

    @Test
    @DisplayName("Should return EmailStrategy when requested")
    void shouldReturnEmailStrategy() {
        NotificationStrategy strategy = NotificationStrategyFactory.getStrategy(NotificationType.EMAIL);
        assertInstanceOf(EmailStrategy.class, strategy, "Result should be an instance of EmailStrategy");
    }

    @Test
    @DisplayName("Should return SMSStrategy when requested")
    void shouldReturnSMSStrategy() {
        NotificationStrategy strategy = NotificationStrategyFactory.getStrategy(NotificationType.SMS);
        assertInstanceOf(SmsStrategy.class, strategy);
    }

    @Test
    @DisplayName("Should return WhatsAppStrategy when requested")
    void shouldReturnWhatsAppStrategy() {
        NotificationStrategy strategy = NotificationStrategyFactory.getStrategy(NotificationType.WHATSAPP);
        assertInstanceOf(WhatsAppStrategy.class, strategy);
    }

    @Test
    @DisplayName("Should throw exception for null notification type")
    void shouldThrowExceptionOnIllegalArgumentType() {
        assertThrows(IllegalArgumentException.class, () -> {
            NotificationStrategyFactory.getStrategy(null);
        });
    }

}
