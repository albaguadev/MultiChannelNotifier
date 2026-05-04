package com.notifier.service;

import com.notifier.model.NotificationRecord;
import com.notifier.model.NotificationStatus;
import com.notifier.model.NotificationType;
import com.notifier.port.PersistencePort;
import net.jqwik.api.*;
import org.junit.jupiter.api.Tag;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Property-based test for {@link QueryService#query(NotificationType, NotificationStatus, Instant, Instant)}
 * when invoked with no filters (equivalent to findAll).
 * <p>
 * <b>Property 5: Ordenación descendente por timestamp</b>
 * <p>
 * For any list of {@link NotificationRecord} instances with distinct timestamps,
 * when {@code QueryService.query(null, null, null, null)} is invoked,
 * the returned list MUST be ordered by timestamp in descending order
 * (most recent first).
 * <p>
 * This test generates arbitrary lists of records with random timestamps,
 * configures a mock {@link PersistencePort} to return them in arbitrary order,
 * and verifies that the service returns them sorted correctly.
 * <p>
 * Validates: Requirement 3.1
 */
@Tag("Feature: notification-persistence")
@Tag("Property 5: Ordenación descendente por timestamp")
class FindAllOrderingTest {

    /**
     * Property: For any list of NotificationRecord with distinct timestamps,
     * QueryService.query(null, null, null, null) returns them ordered by timestamp descending.
     */
    @Property(tries = 100)
    void findAll_shouldReturnRecordsOrderedByTimestampDescending(
            @ForAll("notificationRecordsWithDistinctTimestamps") List<NotificationRecord> records) {

        // Arrange: mock PersistencePort to return records in arbitrary order
        PersistencePort mockPort = mock(PersistencePort.class);
        when(mockPort.findAll()).thenReturn(new ArrayList<>(records));

        QueryService queryService = new QueryService(mockPort);

        // Act: invoke query with no filters (equivalent to findAll)
        List<NotificationRecord> result = queryService.query(null, null, null, null);

        // Assert: result must be ordered by timestamp descending
        assertThat(result).hasSize(records.size());

        for (int i = 0; i < result.size() - 1; i++) {
            Instant current = result.get(i).getTimestamp();
            Instant next = result.get(i + 1).getTimestamp();
            assertThat(current).as("Record at index %d should have timestamp >= record at index %d", i, i + 1)
                    .isAfterOrEqualTo(next);
        }
    }

    /**
     * Arbitrary provider for lists of NotificationRecord with distinct timestamps.
     * <p>
     * Generates lists of size 2 to 20, where each record has a unique timestamp
     * to ensure strict ordering can be verified.
     */
    @Provide
    Arbitrary<List<NotificationRecord>> notificationRecordsWithDistinctTimestamps() {
        return Arbitraries.integers().between(2, 20).flatMap(size -> {
            // Generate 'size' distinct timestamps
            Arbitrary<List<Long>> timestamps = Arbitraries.longs()
                    .between(1_000_000_000_000L, 2_000_000_000_000L) // Valid epoch millis range
                    .list().ofSize(size).uniqueElements();

            return timestamps.map(timestampList -> {
                List<NotificationRecord> records = new ArrayList<>();
                for (Long epochMilli : timestampList) {
                    Instant timestamp = Instant.ofEpochMilli(epochMilli);
                    records.add(createRecordWithTimestamp(timestamp));
                }
                return records;
            });
        });
    }

    /**
     * Helper method to create a NotificationRecord with a specific timestamp.
     */
    private NotificationRecord createRecordWithTimestamp(Instant timestamp) {
        return new NotificationRecord(
                UUID.randomUUID().toString(),
                NotificationType.EMAIL,
                "test@example.com",
                "Test message",
                "Test subject",
                NotificationStatus.SENT,
                null,
                timestamp
        );
    }
}
