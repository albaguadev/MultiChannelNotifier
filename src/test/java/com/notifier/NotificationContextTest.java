package com.notifier;

import com.notifier.context.NotificationContext;
import com.notifier.factory.NotificationStrategyFactory;
import com.notifier.model.NotificationType;
import com.notifier.strategy.NotificationStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NotificationContextTest {

    @Test
    @DisplayName("Context should correctly hold the assigned strategy")
    void shouldSetStrategyCorrectly() {
        NotificationContext context = new NotificationContext();
        NotificationStrategy email = NotificationStrategyFactory.getStrategy(NotificationType.EMAIL);

        context.setStrategy(email);

        assertDoesNotThrow(() -> context.executeStrategy("Test message"));
    }

}
