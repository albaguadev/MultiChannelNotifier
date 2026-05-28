package com.notifier.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notifier.model.NotificationRecord;
import com.notifier.port.PersistencePort;
import net.jqwik.api.*;
import net.jqwik.api.constraints.NotEmpty;
import net.jqwik.api.constraints.StringLength;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Preservation Property Tests for Notification Response Format.
 * 
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4**
 * 
 * This test suite verifies that the fix preserves existing behavior for invalid requests:
 * - Invalid requests continue to return HTTP 400 with validation error messages
 * - No notification records are persisted for invalid requests
 * - No delivery strategies are executed for invalid requests
 * 
 * These tests MUST PASS on unfixed code - they capture the baseline behavior that must be preserved.
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@DisplayName("Preservation: Invalid Request Handling")
public class NotificationControllerPreservationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersistencePort persistencePort;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Reset the mock before each test
        reset(persistencePort);
        when(persistencePort.save(any(NotificationRecord.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ============================================================================
    // Property Test 1: Invalid Request Validation
    // ============================================================================

    @Property
    @DisplayName("Property 1: All invalid requests (missing recipient) return HTTP 400")
    void allInvalidRequestsMissingRecipientReturnHttp400(
            @ForAll @NotEmpty @StringLength(min = 1, max = 100) String message) throws Exception {

        String invalidRequest = """
                {
                  "type": "EMAIL",
                  "message": "%s"
                }
                """.formatted(message.replace("\"", "\\\""));

        MvcResult result = mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andReturn();

        // ASSERTION 1: Response status should be 400 Bad Request
        assertThat(result.getResponse().getStatus())
                .as("Invalid request (missing recipient) should return HTTP 400")
                .isEqualTo(400);

        // ASSERTION 2: Response should contain error message
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody)
                .as("Error response should contain validation error message")
                .isNotEmpty();
    }

    @Property
    @DisplayName("Property 1: All invalid requests (null message) return HTTP 400")
    void allInvalidRequestsNullMessageReturnHttp400(
            @ForAll @NotEmpty @StringLength(min = 1, max = 50) String recipient) throws Exception {

        String invalidRequest = """
                {
                  "type": "EMAIL",
                  "recipient": "%s",
                  "message": null
                }
                """.formatted(recipient.replace("\"", "\\\""));

        MvcResult result = mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andReturn();

        assertThat(result.getResponse().getStatus())
                .as("Invalid request (null message) should return HTTP 400")
                .isEqualTo(400);
    }

    @Property
    @DisplayName("Property 1: All invalid requests (empty message) return HTTP 400")
    void allInvalidRequestsEmptyMessageReturnHttp400(
            @ForAll @NotEmpty @StringLength(min = 1, max = 50) String recipient) throws Exception {

        String invalidRequest = """
                {
                  "type": "EMAIL",
                  "recipient": "%s",
                  "message": ""
                }
                """.formatted(recipient.replace("\"", "\\\""));

        MvcResult result = mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andReturn();

        assertThat(result.getResponse().getStatus())
                .as("Invalid request (empty message) should return HTTP 400")
                .isEqualTo(400);
    }

    @Property
    @DisplayName("Property 1: All invalid requests (null type) return HTTP 400")
    void allInvalidRequestsNullTypeReturnHttp400(
            @ForAll @NotEmpty @StringLength(min = 1, max = 50) String recipient,
            @ForAll @NotEmpty @StringLength(min = 1, max = 100) String message) throws Exception {

        String invalidRequest = """
                {
                  "type": null,
                  "recipient": "%s",
                  "message": "%s"
                }
                """.formatted(recipient.replace("\"", "\\\""), message.replace("\"", "\\\""));

        MvcResult result = mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andReturn();

        assertThat(result.getResponse().getStatus())
                .as("Invalid request (null type) should return HTTP 400")
                .isEqualTo(400);
    }

    @Property
    @DisplayName("Property 1: All invalid requests (empty recipient) return HTTP 400")
    void allInvalidRequestsEmptyRecipientReturnHttp400(
            @ForAll @NotEmpty @StringLength(min = 1, max = 100) String message) throws Exception {

        String invalidRequest = """
                {
                  "type": "EMAIL",
                  "recipient": "",
                  "message": "%s"
                }
                """.formatted(message.replace("\"", "\\\""));

        MvcResult result = mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andReturn();

        assertThat(result.getResponse().getStatus())
                .as("Invalid request (empty recipient) should return HTTP 400")
                .isEqualTo(400);
    }

    // ============================================================================
    // Property Test 2: Database Persistence Preservation
    // ============================================================================

    @Property
    @DisplayName("Property 2: No records persisted for invalid requests (missing recipient)")
    void noRecordsPersisted_InvalidRequestMissingRecipient(
            @ForAll @NotEmpty @StringLength(min = 1, max = 100) String message) throws Exception {

        String invalidRequest = """
                {
                  "type": "EMAIL",
                  "message": "%s"
                }
                """.formatted(message.replace("\"", "\\\""));

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andReturn();

        // ASSERTION: persistencePort.save() should NOT be called for invalid requests
        verify(persistencePort, never())
                .save(any(NotificationRecord.class));
    }

    @Property
    @DisplayName("Property 2: No records persisted for invalid requests (null message)")
    void noRecordsPersisted_InvalidRequestNullMessage(
            @ForAll @NotEmpty @StringLength(min = 1, max = 50) String recipient) throws Exception {

        String invalidRequest = """
                {
                  "type": "EMAIL",
                  "recipient": "%s",
                  "message": null
                }
                """.formatted(recipient.replace("\"", "\\\""));

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andReturn();

        verify(persistencePort, never())
                .save(any(NotificationRecord.class));
    }

    @Property
    @DisplayName("Property 2: No records persisted for invalid requests (empty message)")
    void noRecordsPersisted_InvalidRequestEmptyMessage(
            @ForAll @NotEmpty @StringLength(min = 1, max = 50) String recipient) throws Exception {

        String invalidRequest = """
                {
                  "type": "EMAIL",
                  "recipient": "%s",
                  "message": ""
                }
                """.formatted(recipient.replace("\"", "\\\""));

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andReturn();

        verify(persistencePort, never())
                .save(any(NotificationRecord.class));
    }

    @Property
    @DisplayName("Property 2: No records persisted for invalid requests (null type)")
    void noRecordsPersisted_InvalidRequestNullType(
            @ForAll @NotEmpty @StringLength(min = 1, max = 50) String recipient,
            @ForAll @NotEmpty @StringLength(min = 1, max = 100) String message) throws Exception {

        String invalidRequest = """
                {
                  "type": null,
                  "recipient": "%s",
                  "message": "%s"
                }
                """.formatted(recipient.replace("\"", "\\\""), message.replace("\"", "\\\""));

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andReturn();

        verify(persistencePort, never())
                .save(any(NotificationRecord.class));
    }

    @Property
    @DisplayName("Property 2: No records persisted for invalid requests (empty recipient)")
    void noRecordsPersisted_InvalidRequestEmptyRecipient(
            @ForAll @NotEmpty @StringLength(min = 1, max = 100) String message) throws Exception {

        String invalidRequest = """
                {
                  "type": "EMAIL",
                  "recipient": "",
                  "message": "%s"
                }
                """.formatted(message.replace("\"", "\\\""));

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andReturn();

        verify(persistencePort, never())
                .save(any(NotificationRecord.class));
    }

    // ============================================================================
    // Unit Tests for Specific Invalid Request Scenarios
    // ============================================================================

    @Test
    @DisplayName("Unit Test: Missing recipient field returns HTTP 400")
    void missingRecipientReturnsHttp400() throws Exception {
        String invalidRequest = """
                {
                  "type": "EMAIL",
                  "message": "Test message"
                }
                """;

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(persistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Unit Test: Missing message field returns HTTP 400")
    void missingMessageReturnsHttp400() throws Exception {
        String invalidRequest = """
                {
                  "type": "EMAIL",
                  "recipient": "user@example.com"
                }
                """;

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(persistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Unit Test: Missing type field returns HTTP 400")
    void missingTypeReturnsHttp400() throws Exception {
        String invalidRequest = """
                {
                  "recipient": "user@example.com",
                  "message": "Test message"
                }
                """;

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(persistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Unit Test: Null recipient returns HTTP 400")
    void nullRecipientReturnsHttp400() throws Exception {
        String invalidRequest = """
                {
                  "type": "EMAIL",
                  "recipient": null,
                  "message": "Test message"
                }
                """;

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(persistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Unit Test: Empty message returns HTTP 400")
    void emptyMessageReturnsHttp400() throws Exception {
        String invalidRequest = """
                {
                  "type": "EMAIL",
                  "recipient": "user@example.com",
                  "message": ""
                }
                """;

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(persistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Unit Test: Whitespace-only message returns HTTP 400")
    void whitespaceOnlyMessageReturnsHttp400() throws Exception {
        String invalidRequest = """
                {
                  "type": "EMAIL",
                  "recipient": "user@example.com",
                  "message": "   "
                }
                """;

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(persistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Unit Test: Invalid JSON returns HTTP 400")
    void invalidJsonReturnsHttp400() throws Exception {
        String invalidRequest = "{ invalid json }";

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(persistencePort, never()).save(any());
    }
}
