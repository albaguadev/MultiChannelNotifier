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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Property-based test for {@link QueryService} filtering by date range.
 * <p>
 * <b>Property 7: Corrección del filtrado por rango de fechas</b>
 * <p>
 * For any set of {@link NotificationRecord} instances with varied timestamps,
 * when filtering by a date range {@code [from, to]}, the returned list MUST contain
 * all and only those records whose timestamp falls within the range (both bounds inclusive).
 * <p>
 * This test verifies two key properties:
 * <ul>
 *   <li><b>Completeness</b>: No record with timestamp in {@code [from, to]} is excluded</li>
 *   <li><b>Correctness</b>: No record with timestamp outside {@code [from, to]} is included</li>
 * </ul>
 * <p>
 * <b>Validates: Requirements 3.4, 4.4</b>
 */
@Tag("Feature: notification-persistence")
@Tag("Property 7: Corrección del filtrado por rango de fechas")
class FilterByDateRangeTest {

    /**
     * Property: For any set of NotificationRecord with varied timestamps,
     * when filtering by a date range [from, to], the result must contain
     * all and only records whose timestamp is within the range (inclusive).
     */
    @Property(tries = 100)
    @Label("Feature: notification-persistence, Property 7: Corrección del filtrado por rango de fechas")
    void findByDateRange_shouldReturnAllAndOnlyRecordsWithinRange(
            @ForAll("notificationRecordsWithVariedTimestamps") List<NotificationRecord> allRecords,
            @ForAll("dateRange") DateRange range) {

        Instant from = range.from();
        Instant to = range.to();

        // Arrange: compute expected result (all records with timestamp in [from, to])
        List<NotificationRecord> expectedRecords = allRecords.stream()
                .filter(record -> {
                    Instant timestamp = record.getTimestamp();
                    return !timestamp.isBefore(from) && !timestamp.isAfter(to);
                })
                .collect(Collectors.toList());

        // Mock PersistencePort to return the filtered subset
        PersistencePort mockPort = mock(PersistencePort.class);
        when(mockPort.findByTimestampBetween(from, to)).thenReturn(new ArrayList<>(expectedRecords));

        QueryService queryService = new QueryService(mockPort);

        // Act: invoke query with date range filter
        List<NotificationRecord> result = queryService.query(null, null, from, to);

        // Assert: result must match expected records exactly
        assertThat(result)
                .as("Result size must match expected size")
                .hasSize(expectedRecords.size());

        // Correctness: all returned records must have timestamp within [from, to]
        assertThat(result)
                .as("All returned records must have timestamp within [%s, %s]", from, to)
                .allMatch(record -> {
                    Instant timestamp = record.getTimestamp();
                    return !timestamp.isBefore(from) && !timestamp.isAfter(to);
                });

        // Completeness: all expected records must be present in the result
        assertThat(result)
                .as("All records with timestamp in [%s, %s] must be included", from, to)
                .containsExactlyInAnyOrderElementsOf(expectedRecords);
    }

    /**
     * Arbitrary provider for lists of NotificationRecord with varied timestamps.
     * <p>
     * Generates lists of size 5 to 30, where records have timestamps distributed
     * across a wide range to ensure filtering logic is properly tested.
     */
    @Provide
    Arbitrary<List<NotificationRecord>> notificationRecordsWithVariedTimestamps() {
        return Arbitraries.integers().between(5, 30).flatMap(size -> {
            // Generate timestamps across a wide range (year 2020 to 2025)
            Arbitrary<Instant> timestamps = Arbitraries.longs()
                    .between(1_577_836_800_000L, 1_735_689_600_000L) // 2020-01-01 to 2025-01-01
                    .map(Instant::ofEpochMilli);

            return timestamps.list().ofSize(size).map(timestampList -> {
                List<NotificationRecord> records = new ArrayList<>();
                for (Instant timestamp : timestampList) {
                    records.add(createRecordWithTimestamp(timestamp));
                }
                return records;
            });
        });
    }

    /**
     * Arbitrary provider for date ranges [from, to] where from <= to.
     * <p>
     * Generates arbitrary date ranges within the same epoch as the record timestamps
     * to ensure meaningful filtering tests.
     */
    @Provide
    Arbitrary<DateRange> dateRange() {
        return Arbitraries.longs()
                .between(1_577_836_800_000L, 1_735_689_600_000L) // 2020-01-01 to 2025-01-01
                .flatMap(fromEpoch -> {
                    // Generate 'to' timestamp that is >= 'from'
                    return Arbitraries.longs()
                            .between(fromEpoch, 1_735_689_600_000L)
                            .map(toEpoch -> new DateRange(
                                    Instant.ofEpochMilli(fromEpoch),
                                    Instant.ofEpochMilli(toEpoch)
                            ));
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

    /**
     * Simple record to represent a date range with from and to bounds.
     */
    private record DateRange(Instant from, Instant to) {
    }
}
