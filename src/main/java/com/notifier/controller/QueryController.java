package com.notifier.controller;

import com.notifier.dto.NotificationRecordResponse;
import com.notifier.model.NotificationStatus;
import com.notifier.model.NotificationType;
import com.notifier.service.QueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * REST controller that exposes query endpoints for persisted notification records.
 * <p>
 * All endpoints are mounted under {@code /api/v1/notifications}. The single
 * {@code GET} endpoint supports optional filtering by type, status, or date range.
 * When no filter parameters are provided, all records are returned.
 * </p>
 *
 * <p>Dispatch priority (first matching condition wins):</p>
 * <ol>
 *   <li>Both {@code from} and {@code to} present → filter by date range</li>
 *   <li>{@code type} present → filter by notification type</li>
 *   <li>{@code status} present → filter by notification status</li>
 *   <li>No parameters → return all records</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class QueryController {

    private final QueryService queryService;

    /**
     * Constructs a {@code QueryController} with the given query service.
     *
     * @param queryService the service used to retrieve notification records; must not be {@code null}
     */
    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    /**
     * Retrieves notification records, optionally filtered by type, status, or date range.
     * <p>
     * All parameters are optional. When both {@code from} and {@code to} are provided,
     * the date-range filter takes precedence over {@code type} and {@code status}.
     * An empty list is returned when no records match the given criteria.
     * </p>
     *
     * @param type   optional delivery channel filter (e.g. {@code EMAIL}, {@code SMS})
     * @param status optional delivery outcome filter (e.g. {@code SENT}, {@code FAILED})
     * @param from   optional start of the time range (inclusive), in ISO-8601 format
     * @param to     optional end of the time range (inclusive), in ISO-8601 format
     * @return HTTP 200 with a (possibly empty) list of {@link NotificationRecordResponse}
     */
    @GetMapping
    public ResponseEntity<List<NotificationRecordResponse>> getNotifications(
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {

        List<NotificationRecordResponse> response;

        if (from != null && to != null) {
            response = queryService.findByDateRange(from, to).stream()
                    .map(NotificationRecordResponse::from)
                    .toList();
        } else if (type != null) {
            response = queryService.findByType(type).stream()
                    .map(NotificationRecordResponse::from)
                    .toList();
        } else if (status != null) {
            response = queryService.findByStatus(status).stream()
                    .map(NotificationRecordResponse::from)
                    .toList();
        } else {
            response = queryService.findAll().stream()
                    .map(NotificationRecordResponse::from)
                    .toList();
        }

        return ResponseEntity.ok(response);
    }
}
