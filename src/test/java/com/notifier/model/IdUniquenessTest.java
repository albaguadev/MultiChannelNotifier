package com.notifier.model;

import com.notifier.dto.NotificationRequest;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for {@link NotificationRecord} ID uniqueness.
 * <p>
 * This test suite verifies that every notification record receives a unique identifier,
 * regardless of whether it's created via {@link NotificationRecord#ofSuccess} or
 * {@link NotificationRecord#ofFailure}.
 * </p>
 * <p>
 * Feature: notification-persistence<br>
 * Property 3: Unicidad de identificadores
 * </p>
 * <p>
 * Validates requirements: 1.3, 5.4
 * </p>
 */
class IdUniquenessTest {

    /**
     * Property: Every notification record must have a unique identifier.
     * <p>
     * Given: N ≥ 2 valid NotificationRequests<br>
     * When: Creating N NotificationRecords (mix of success and failure)<br>
     * Then: All IDs must be distinct from each other
     * </p>
     */
    @Property(tries = 100)
    @Label("Feature: notification-persistence, Property 3: Unicidad de identificadores")
    void allRecordsHaveUniqueIds(
            @ForAll("notificationRequestLists") List<NotificationRequest> requests) {
        
        Assume.that(requests.size() >= 2);

        // When: Creating multiple notification records (mix of success and failure)
        List<NotificationRecord> records = requests.stream()
                .map(request -> {
                    // Alternate between success and failure records
                    if (requests.indexOf(request) % 2 == 0) {
                        return NotificationRecord.ofSuccess(request);
                    } else {
                        return NotificationRecord.ofFailure(request, "Test error");
                    }
                })
                .collect(Collectors.toList());

        // Then: All IDs are unique
        Set<String> ids = records.stream()
                .map(NotificationRecord::getId)
                .collect(Collectors.toSet());

        assertThat(ids)
                .as("All notification record IDs must be unique")
                .hasSize(records.size());

        // And: No ID is null or empty
        assertThat(records)
                .as("All IDs must be non-null and non-empty")
                .allMatch(record -> record.getId() != null && !record.getId().isBlank());
    }

    /**
     * Property: IDs are unique even when creating records from identical requests.
     * <p>
     * Given: The same NotificationRequest used multiple times<br>
     * When: Creating multiple NotificationRecords from the same request<br>
     * Then: Each record must have a different ID
     * </p>
     */
    @Property(tries = 100)
    @Label("Feature: notification-persistence, Property 3: IDs únicos para requests idénticas")
    void identicalRequestsProduceDifferentIds(
            @ForAll("notificationRequests") NotificationRequest request,
            @ForAll @IntRange(min = 2, max = 20) int count) {
        
        // When: Creating multiple records from the same request
        List<NotificationRecord> records = java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> NotificationRecord.ofSuccess(request))
                .collect(Collectors.toList());

        // Then: All IDs are unique
        Set<String> ids = records.stream()
                .map(NotificationRecord::getId)
                .collect(Collectors.toSet());

        assertThat(ids)
                .as("Records from identical requests must have unique IDs")
                .hasSize(count);
    }

    /**
     * Property: IDs follow UUID format (basic validation).
     * <p>
     * Given: Any valid NotificationRequest<br>
     * When: Creating a NotificationRecord<br>
     * Then: The ID should follow UUID string format (36 characters with hyphens)
     * </p>
     */
    @Property(tries = 100)
    @Label("Feature: notification-persistence, Property 3: IDs siguen formato UUID")
    void idsFollowUuidFormat(
            @ForAll("notificationRequests") NotificationRequest request) {
        
        // When: Creating a notification record
        NotificationRecord record = NotificationRecord.ofSuccess(request);

        // Then: ID follows UUID format
        String id = record.getId();
        
        assertThat(id)
                .as("ID should be 36 characters long (UUID format)")
                .hasSize(36);

        assertThat(id)
                .as("ID should match UUID pattern")
                .matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    /**
     * Provides lists of arbitrary valid {@link NotificationRequest} instances.
     */
    @Provide
    Arbitrary<List<NotificationRequest>> notificationRequestLists() {
        return notificationRequests().list().ofMinSize(2).ofMaxSize(50);
    }

    /**
     * Provides arbitrary valid {@link NotificationRequest} instances.
     */
    @Provide
    Arbitrary<NotificationRequest> notificationRequests() {
        Arbitrary<NotificationType> types = Arbitraries.of(NotificationType.values());
        
        Arbitrary<String> recipients = Arbitraries.oneOf(
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20).map(s -> s + "@example.com"),
                Arbitraries.strings().numeric().ofLength(9).map(s -> "+34" + s)
        );

        Arbitrary<String> messages = Arbitraries.strings()
                .alpha().numeric().withChars(' ')
                .ofMinLength(1).ofMaxLength(500);

        Arbitrary<String> subjects = Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(100)
        );

        return Combinators.combine(types, recipients, messages, subjects)
                .as((type, recipient, message, subject) -> {
                    NotificationRequest request = new NotificationRequest();
                    request.setType(type);
                    request.setRecipient(recipient);
                    request.setMessage(message);
                    request.setSubject(subject);
                    return request;
                });
    }
}
