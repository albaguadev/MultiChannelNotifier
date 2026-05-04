package com.notifier.service;

import com.notifier.dto.NotificationRequest;
import com.notifier.factory.NotificationStrategyFactory;
import com.notifier.model.NotificationType;
import com.notifier.port.PersistencePort;
import com.notifier.strategy.NotificationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link NotificationService} persistence behavior.
 * <p>
 * This test suite verifies that persistence failures do not interrupt the notification
 * sending flow and that errors are properly logged without propagating exceptions.
 * </p>
 * <p>
 * Validates requirement: 1.5 - "IF el NotificationRepository no puede persistir el registro
 * por un fallo de infraestructura, THEN THE Sistema SHALL registrar el error en el log de
 * la aplicación sin interrumpir la respuesta al cliente."
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class NotificationServicePersistenceTest {

    @Mock
    private NotificationStrategyFactory factory;

    @Mock
    private PersistencePort persistencePort;

    @Mock
    private NotificationStrategy strategy;

    @Captor
    private ArgumentCaptor<com.notifier.model.NotificationRecord> recordCaptor;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(factory, persistencePort);
    }

    /**
     * Test: persistSafely does not propagate exceptions from PersistencePort.
     * <p>
     * Given: A valid NotificationRequest and a PersistencePort that throws an exception<br>
     * When: sendNotification is called and the notification is sent successfully<br>
     * Then: The method completes without throwing a persistence exception
     * </p>
     * <p>
     * This test verifies Requirement 1.5: persistence failures must not interrupt
     * the response to the client.
     * </p>
     */
    @Test
    void persistSafely_doesNotPropagateExceptionsFromPersistencePort() {
        // Given: A valid notification request
        NotificationRequest request = new NotificationRequest();
        request.setType(NotificationType.EMAIL);
        request.setRecipient("user@example.com");
        request.setMessage("Test message");
        request.setSubject("Test subject");

        // And: The factory returns a valid strategy
        when(factory.getStrategy(NotificationType.EMAIL)).thenReturn(strategy);

        // And: The strategy validates and sends successfully (no exceptions)
        doNothing().when(strategy).validate(request);
        doNothing().when(strategy).send(request);

        // And: The persistence port throws an exception when save is called
        when(persistencePort.save(any())).thenThrow(
                new RuntimeException("Database connection failed")
        );

        // When: sendNotification is called
        // Then: No exception is propagated to the caller
        assertThatCode(() -> notificationService.sendNotification(request))
                .as("Persistence failures must not propagate to the caller")
                .doesNotThrowAnyException();

        // And: The persistence port was called (attempt was made)
        verify(persistencePort, times(1)).save(any());
    }

    /**
     * Test: persistSafely attempts to save the record even when persistence fails.
     * <p>
     * Given: A valid NotificationRequest and a PersistencePort that throws an exception<br>
     * When: sendNotification is called<br>
     * Then: The persistence port's save method is invoked with a NotificationRecord
     * </p>
     */
    @Test
    void persistSafely_attemptsToSaveRecordDespiteFailure() {
        // Given: A valid notification request
        NotificationRequest request = new NotificationRequest();
        request.setType(NotificationType.SMS);
        request.setRecipient("+34123456789");
        request.setMessage("SMS test message");

        // And: The factory returns a valid strategy
        when(factory.getStrategy(NotificationType.SMS)).thenReturn(strategy);

        // And: The strategy validates and sends successfully
        doNothing().when(strategy).validate(request);
        doNothing().when(strategy).send(request);

        // And: The persistence port throws an exception
        when(persistencePort.save(any())).thenThrow(
                new RuntimeException("Disk full")
        );

        // When: sendNotification is called
        assertThatCode(() -> notificationService.sendNotification(request))
                .doesNotThrowAnyException();

        // Then: The persistence port's save method was called
        verify(persistencePort, times(1)).save(recordCaptor.capture());

        // And: The captured record has the correct data
        com.notifier.model.NotificationRecord capturedRecord = recordCaptor.getValue();
        assertThat(capturedRecord.getType()).isEqualTo(NotificationType.SMS);
        assertThat(capturedRecord.getRecipient()).isEqualTo("+34123456789");
        assertThat(capturedRecord.getMessage()).isEqualTo("SMS test message");
        assertThat(capturedRecord.getStatus()).isEqualTo(com.notifier.model.NotificationStatus.SENT);
    }

    /**
     * Test: persistSafely does not propagate exceptions even when notification sending fails.
     * <p>
     * Given: A NotificationRequest that causes the strategy to throw an exception<br>
     * And: The persistence port also throws an exception<br>
     * When: sendNotification is called<br>
     * Then: Only the sending exception is propagated, not the persistence exception
     * </p>
     */
    @Test
    void persistSafely_doesNotPropagateExceptionsWhenSendingAlsoFails() {
        // Given: A valid notification request
        NotificationRequest request = new NotificationRequest();
        request.setType(NotificationType.WHATSAPP);
        request.setRecipient("+34987654321");
        request.setMessage("WhatsApp test message");

        // And: The factory returns a valid strategy
        when(factory.getStrategy(NotificationType.WHATSAPP)).thenReturn(strategy);

        // And: The strategy validates successfully but sending fails
        doNothing().when(strategy).validate(request);
        doThrow(new RuntimeException("WhatsApp service unavailable"))
                .when(strategy).send(request);

        // And: The persistence port also throws an exception
        when(persistencePort.save(any())).thenThrow(
                new RuntimeException("Database connection timeout")
        );

        // When: sendNotification is called
        // Then: Only the sending exception is propagated
        assertThatCode(() -> notificationService.sendNotification(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("WhatsApp service unavailable");

        // And: The persistence port was still called (in the finally block)
        verify(persistencePort, times(1)).save(recordCaptor.capture());

        // And: The captured record has FAILED status
        com.notifier.model.NotificationRecord capturedRecord = recordCaptor.getValue();
        assertThat(capturedRecord.getStatus()).isEqualTo(com.notifier.model.NotificationStatus.FAILED);
        assertThat(capturedRecord.getErrorMessage()).isEqualTo("WhatsApp service unavailable");
    }

    /**
     * Test: persistSafely successfully saves when no exception occurs.
     * <p>
     * Given: A valid NotificationRequest and a working PersistencePort<br>
     * When: sendNotification is called<br>
     * Then: The record is successfully persisted
     * </p>
     */
    @Test
    void persistSafely_successfullySavesWhenNoExceptionOccurs() {
        // Given: A valid notification request
        NotificationRequest request = new NotificationRequest();
        request.setType(NotificationType.EMAIL);
        request.setRecipient("success@example.com");
        request.setMessage("Success test message");
        request.setSubject("Success subject");

        // And: The factory returns a valid strategy
        when(factory.getStrategy(NotificationType.EMAIL)).thenReturn(strategy);

        // And: The strategy validates and sends successfully
        doNothing().when(strategy).validate(request);
        doNothing().when(strategy).send(request);

        // And: The persistence port saves successfully
        when(persistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When: sendNotification is called
        assertThatCode(() -> notificationService.sendNotification(request))
                .doesNotThrowAnyException();

        // Then: The persistence port's save method was called successfully
        verify(persistencePort, times(1)).save(recordCaptor.capture());

        // And: The captured record has SENT status
        com.notifier.model.NotificationRecord capturedRecord = recordCaptor.getValue();
        assertThat(capturedRecord.getStatus()).isEqualTo(com.notifier.model.NotificationStatus.SENT);
        assertThat(capturedRecord.getErrorMessage()).isNull();
    }
}
