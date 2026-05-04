package com.notifier.service;

import com.notifier.exception.PersistenceException;
import com.notifier.model.NotificationRecord;
import com.notifier.model.NotificationStatus;
import com.notifier.model.NotificationType;
import com.notifier.port.PersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link QueryService} exception propagation behavior.
 * <p>
 * This test suite verifies that persistence failures during read operations
 * are properly wrapped and propagated as {@link PersistenceException}, allowing
 * callers to distinguish infrastructure failures from empty result sets.
 * </p>
 * <p>
 * Validates requirement: 3.6 - "IF el NotificationRepository no puede recuperar
 * los registros por un fallo de infraestructura, THEN THE QueryService SHALL
 * propagar una excepción tipificada que permita al llamador distinguir el fallo
 * de persistencia de un resultado vacío."
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class QueryServiceExceptionTest {

    @Mock
    private PersistencePort persistencePort;

    private QueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = new QueryService(persistencePort);
    }

    /**
     * Test: findAll propagates PersistenceException when persistence layer fails.
     * <p>
     * Given: A PersistencePort that throws a RuntimeException when findAll is called<br>
     * When: QueryService.query() is called without filters (which invokes findAll)<br>
     * Then: A PersistenceException is thrown with a descriptive message
     * </p>
     * <p>
     * This test verifies Requirement 3.6: infrastructure failures must be propagated
     * as a typed exception to distinguish them from empty results.
     * </p>
     */
    @Test
    void findAll_propagatesPersistenceException_whenPersistenceLayerFails() {
        // Given: The persistence port throws a RuntimeException when findAll is called
        when(persistencePort.findAll()).thenThrow(
                new RuntimeException("Database connection timeout")
        );

        // When: query is called without filters (which invokes findAll internally)
        // Then: A PersistenceException is thrown
        assertThatThrownBy(() -> queryService.query(null, null, null, null))
                .isInstanceOf(PersistenceException.class)
                .hasMessageContaining("Failed to retrieve all notification records")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    /**
     * Test: findAll propagates existing PersistenceException without wrapping.
     * <p>
     * Given: A PersistencePort that throws a PersistenceException when findAll is called<br>
     * When: QueryService.query() is called without filters<br>
     * Then: The original PersistenceException is propagated without additional wrapping
     * </p>
     */
    @Test
    void findAll_propagatesExistingPersistenceException_withoutWrapping() {
        // Given: The persistence port throws a PersistenceException
        PersistenceException originalException = new PersistenceException(
                "Database unavailable",
                new RuntimeException("Connection refused")
        );
        when(persistencePort.findAll()).thenThrow(originalException);

        // When: query is called without filters
        // Then: The same PersistenceException is propagated
        assertThatThrownBy(() -> queryService.query(null, null, null, null))
                .isSameAs(originalException);
    }

    /**
     * Test: findByType propagates PersistenceException when persistence layer fails.
     * <p>
     * Given: A PersistencePort that throws a RuntimeException when findByType is called<br>
     * When: QueryService.query() is called with a type filter<br>
     * Then: A PersistenceException is thrown with a descriptive message including the type
     * </p>
     */
    @Test
    void findByType_propagatesPersistenceException_whenPersistenceLayerFails() {
        // Given: The persistence port throws a RuntimeException when findByType is called
        when(persistencePort.findByType(NotificationType.EMAIL)).thenThrow(
                new RuntimeException("Query execution failed")
        );

        // When: query is called with a type filter
        // Then: A PersistenceException is thrown
        assertThatThrownBy(() -> queryService.query(NotificationType.EMAIL, null, null, null))
                .isInstanceOf(PersistenceException.class)
                .hasMessageContaining("Failed to retrieve notification records by type")
                .hasMessageContaining("EMAIL")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    /**
     * Test: findByStatus propagates PersistenceException when persistence layer fails.
     * <p>
     * Given: A PersistencePort that throws a RuntimeException when findByStatus is called<br>
     * When: QueryService.query() is called with a status filter<br>
     * Then: A PersistenceException is thrown with a descriptive message including the status
     * </p>
     */
    @Test
    void findByStatus_propagatesPersistenceException_whenPersistenceLayerFails() {
        // Given: The persistence port throws a RuntimeException when findByStatus is called
        when(persistencePort.findByStatus(NotificationStatus.FAILED)).thenThrow(
                new RuntimeException("Index corruption detected")
        );

        // When: query is called with a status filter
        // Then: A PersistenceException is thrown
        assertThatThrownBy(() -> queryService.query(null, NotificationStatus.FAILED, null, null))
                .isInstanceOf(PersistenceException.class)
                .hasMessageContaining("Failed to retrieve notification records by status")
                .hasMessageContaining("FAILED")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    /**
     * Test: findByDateRange propagates PersistenceException when persistence layer fails.
     * <p>
     * Given: A PersistencePort that throws a RuntimeException when findByTimestampBetween is called<br>
     * When: QueryService.query() is called with a date range filter<br>
     * Then: A PersistenceException is thrown with a descriptive message including the date range
     * </p>
     */
    @Test
    void findByDateRange_propagatesPersistenceException_whenPersistenceLayerFails() {
        // Given: A date range
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-01-31T23:59:59Z");

        // And: The persistence port throws a RuntimeException when findByTimestampBetween is called
        when(persistencePort.findByTimestampBetween(from, to)).thenThrow(
                new RuntimeException("Disk I/O error")
        );

        // When: query is called with a date range filter
        // Then: A PersistenceException is thrown
        assertThatThrownBy(() -> queryService.query(null, null, from, to))
                .isInstanceOf(PersistenceException.class)
                .hasMessageContaining("Failed to retrieve notification records for date range")
                .hasMessageContaining(from.toString())
                .hasMessageContaining(to.toString())
                .hasCauseInstanceOf(RuntimeException.class);
    }
}
