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
 * Property-based test for {@link QueryService} filtering by type and status.
 * <p>
 * <b>Property 6: Corrección del filtrado por tipo y estado</b>
 * <p>
 * For any set of {@link NotificationRecord} instances with varied types and statuses,
 * when filtering by a specific type or status, the returned list MUST contain all
 * and only those records that satisfy the filter criteria.
 * <p>
 * This test verifies two key properties:
 * <ul>
 *   <li><b>Completeness</b>: No record satisfying the filter is excluded</li>
 *   <li><b>Correctness</b>: No record violating the filter is included</li>
 * </ul>
 * <p>
 * <b>Validates: Requirements 3.2, 3.3, 4.2, 4.3</b>
 */
@Tag("Feature: notification-persistence")
@Tag("Property 6: Corrección del filtrado por tipo y estado")
class FilterByTypeAndStatusTest {

    /**
     * Property: For any set of NotificationRecord with varied types,
     * when filtering by a specific NotificationType, the result must contain
     * all and only records with that type.
     */
    @Property(tries = 100)
    @Label("Feature: notification-persistence, Property 6: Corrección del filtrado por tipo y estado")
    void findByType_shouldReturnAllAndOnlyRecordsWithSpecifiedType(
            @ForAll("notificationRecordsWithVariedTypes") List<NotificationRecord> allRecords,
            @ForAll NotificationType filterType) {

        // Arrange: compute expected result (all records with the specified type)
        List<NotificationRecord> expectedRecords = allRecords.stream()
                .filter(record -> record.getType() == filterType)
                .collect(Collectors.toList());

        // Mock PersistencePort to return the filtered subset
        PersistencePort mockPort = mock(PersistencePort.class);
        when(mockPort.findByType(filterType)).thenReturn(new ArrayList<>(expectedRecords));

        QueryService queryService = new QueryService(mockPort);

        // Act: invoke query with type filter
        List<NotificationRecord> result = queryService.query(filterType, null, null, null);

        // Assert: result must match expected records exactly
        assertThat(result)
                .as("Result size must match expected size")
                .hasSize(expectedRecords.size());

        // Correctness: all returned records must have the specified type
        assertThat(result)
                .as("All returned records must have type %s", filterType)
                .allMatch(record -> record.getType() == filterType);

        // Completeness: all expected records must be present in the result
        assertThat(result)
                .as("All records with type %s must be included", filterType)
                .containsExactlyInAnyOrderElementsOf(expectedRecords);
    }

    /**
     * Property: For any set of NotificationRecord with varied statuses,
     * when filtering by a specific NotificationStatus, the result must contain
     * all and only records with that status.
     */
    @Property(tries = 100)
    @Label("Feature: notification-persistence, Property 6: Corrección del filtrado por tipo y estado")
    void findByStatus_shouldReturnAllAndOnlyRecordsWithSpecifiedStatus(
            @ForAll("notificationRecordsWithVariedStatuses") List<NotificationRecord> allRecords,
            @ForAll NotificationStatus filterStatus) {

        // Arrange: compute expected result (all records with the specified status)
        List<NotificationRecord> expectedRecords = allRecords.stream()
                .filter(record -> record.getStatus() == filterStatus)
                .collect(Collectors.toList());

        // Mock PersistencePort to return the filtered subset
        PersistencePort mockPort = mock(PersistencePort.class);
        when(mockPort.findByStatus(filterStatus)).thenReturn(new ArrayList<>(expectedRecords));

        QueryService queryService = new QueryService(mockPort);

        // Act: invoke query with status filter
        List<NotificationRecord> result = queryService.query(null, filterStatus, null, null);

        // Assert: result must match expected records exactly
        assertThat(result)
                .as("Result size must match expected size")
                .hasSize(expectedRecords.size());

        // Correctness: all returned records must have the specified status
        assertThat(result)
                .as("All returned records must have status %s", filterStatus)
                .allMatch(record -> record.getStatus() == filterStatus);

        // Completeness: all expected records must be present in the result
        assertThat(result)
                .as("All records with status %s must be included", filterStatus)
                .containsExactlyInAnyOrderElementsOf(expectedRecords);
    }

    /**
     * Arbitrary provider for lists of NotificationRecord with varied types.
     * <p>
     * Generates lists of size 5 to 30, where records have random types
     * (EMAIL, SMS, WHATSAPP) to ensure filtering logic is properly tested.
     */
    @Provide
    Arbitrary<List<NotificationRecord>> notificationRecordsWithVariedTypes() {
        return Arbitraries.integers().between(5, 30).flatMap(size -> {
            Arbitrary<NotificationType> types = Arbitraries.of(NotificationType.values());
            Arbitrary<NotificationStatus> statuses = Arbitraries.of(NotificationStatus.values());

            return Combinators.combine(
                    types.list().ofSize(size),
                    statuses.list().ofSize(size)
            ).as((typeList, statusList) -> {
                List<NotificationRecord> records = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    records.add(createRecord(typeList.get(i), statusList.get(i)));
                }
                return records;
            });
        });
    }

    /**
     * Arbitrary provider for lists of NotificationRecord with varied statuses.
     * <p>
     * Generates lists of size 5 to 30, where records have random statuses
     * (SENT, FAILED) to ensure filtering logic is properly tested.
     */
    @Provide
    Arbitrary<List<NotificationRecord>> notificationRecordsWithVariedStatuses() {
        return Arbitraries.integers().between(5, 30).flatMap(size -> {
            Arbitrary<NotificationType> types = Arbitraries.of(NotificationType.values());
            Arbitrary<NotificationStatus> statuses = Arbitraries.of(NotificationStatus.values());

            return Combinators.combine(
                    types.list().ofSize(size),
                    statuses.list().ofSize(size)
            ).as((typeList, statusList) -> {
                List<NotificationRecord> records = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    records.add(createRecord(typeList.get(i), statusList.get(i)));
                }
                return records;
            });
        });
    }

    /**
     * Helper method to create a NotificationRecord with specified type and status.
     */
    private NotificationRecord createRecord(NotificationType type, NotificationStatus status) {
        return new NotificationRecord(
                UUID.randomUUID().toString(),
                type,
                "test@example.com",
                "Test message",
                "Test subject",
                status,
                status == NotificationStatus.FAILED ? "Test error" : null,
                Instant.now()
        );
    }
}
