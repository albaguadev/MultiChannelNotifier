package com.notifier.service;

import com.notifier.dto.NotificationRequest;
import com.notifier.factory.NotificationStrategyFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static NotificationStrategyFactory factory;

    public NotificationService() {

    }

    public void sendNotification(NotificationRequest request) {

    }
}
