package com.notifier.controller;

import com.notifier.dto.NotificationRecordResponse;
import com.notifier.dto.NotificationRequest;
import com.notifier.model.NotificationRecord;
import com.notifier.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Incoming HTTP requests are managed by this REST controller.
 * The endpoint is exposed to facilitate the dispatching of notifications.
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Notification requests are processed via this POST endpoint.
     * Data integrity is verified through the @Valid annotation before execution.
     *
     * @param request The validated notification payload.
     * @return The persisted notification record as a JSON response.
     */
    @PostMapping
    public ResponseEntity<NotificationRecordResponse> sendNotification(@Valid @RequestBody NotificationRequest request) {
        // Logically, the service layer is invoked at this stage.
        // The service returns the persisted notification record.

        NotificationRecord record = notificationService.sendNotification(request);
        NotificationRecordResponse response = NotificationRecordResponse.from(record);
        return ResponseEntity.ok(response);
    }
}