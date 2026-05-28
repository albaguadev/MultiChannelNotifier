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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Bug Condition Exploration Test for Notification Response Format.
 * 
 * **Validates: Requirements 2.1, 2.2**
 * 
 * This test verifies that the POST /api/v1/notifications endpoint returns
 * a JSON response with Content-Type application/json instead of text/plain.
 * 
 * On UNFIXED code, this test MUST FAIL - the failure confirms the bug exists.
 * The bug is that the controller returns ResponseEntity<String> with text/plain
 * instead of ResponseEntity<NotificationRecordResponse> with application/json.
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@DisplayName("Bug Condition Exploration: Notification Response Format")
public class NotificationControllerResponseFormatTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersistencePort persistencePort;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        when(persistencePort.save(any(NotificationRecord.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("Bug Condition: Response Content-Type should be application/json for valid EMAIL request")
    void shouldReturnJsonContentTypeForEmailNotification() throws Exception {
        String validRequest = """
                {
                  "type": "EMAIL",
                  "recipient": "user@example.com",
                  "message": "Hello",
                  "subject": "Test"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest))
                .andExpect(status().isOk())
                .andReturn();

        // ASSERTION 1: Content-Type should be application/json
        String contentType = result.getResponse().getContentType();
        assertThat(contentType)
                .as("Response Content-Type should be application/json, not text/plain")
                .contains("application/json");

        // ASSERTION 2: Response body should be valid JSON (not a plain string)
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody)
                .as("Response body should be a JSON object, not a plain string")
                .startsWith("{")
                .endsWith("}");

        // ASSERTION 3: Parse JSON and verify required fields exist
        var jsonNode = objectMapper.readTree(responseBody);
        assertThat(jsonNode.has("id"))
                .as("Response JSON should contain 'id' field")
                .isTrue();
        assertThat(jsonNode.has("type"))
                .as("Response JSON should contain 'type' field")
                .isTrue();
        assertThat(jsonNode.has("recipient"))
                .as("Response JSON should contain 'recipient' field")
                .isTrue();
        assertThat(jsonNode.has("message"))
                .as("Response JSON should contain 'message' field")
                .isTrue();
        assertThat(jsonNode.has("subject"))
                .as("Response JSON should contain 'subject' field")
                .isTrue();
        assertThat(jsonNode.has("status"))
                .as("Response JSON should contain 'status' field")
                .isTrue();
        assertThat(jsonNode.has("timestamp"))
                .as("Response JSON should contain 'timestamp' field")
                .isTrue();

        // ASSERTION 4: Verify field values
        assertThat(jsonNode.get("type").asText())
                .as("Response type should match request type")
                .isEqualTo("EMAIL");
        assertThat(jsonNode.get("recipient").asText())
                .as("Response recipient should match request recipient")
                .isEqualTo("user@example.com");
        assertThat(jsonNode.get("message").asText())
                .as("Response message should match request message")
                .isEqualTo("Hello");
        assertThat(jsonNode.get("subject").asText())
                .as("Response subject should match request subject")
                .isEqualTo("Test");
        assertThat(jsonNode.get("status").asText())
                .as("Response status should be SENT for successful delivery")
                .isEqualTo("SENT");
    }

    @Test
    @DisplayName("Bug Condition: Response Content-Type should be application/json for valid SMS request")
    void shouldReturnJsonContentTypeForSmsNotification() throws Exception {
        String validRequest = """
                {
                  "type": "SMS",
                  "recipient": "+34912345678",
                  "message": "Hello"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest))
                .andExpect(status().isOk())
                .andReturn();

        String contentType = result.getResponse().getContentType();
        assertThat(contentType)
                .as("Response Content-Type should be application/json for SMS")
                .contains("application/json");

        String responseBody = result.getResponse().getContentAsString();
        var jsonNode = objectMapper.readTree(responseBody);
        assertThat(jsonNode.get("type").asText()).isEqualTo("SMS");
    }

    @Test
    @DisplayName("Bug Condition: Response Content-Type should be application/json for valid WHATSAPP request")
    void shouldReturnJsonContentTypeForWhatsAppNotification() throws Exception {
        String validRequest = """
                {
                  "type": "WHATSAPP",
                  "recipient": "+34912345678",
                  "message": "Hello"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest))
                .andExpect(status().isOk())
                .andReturn();

        String contentType = result.getResponse().getContentType();
        assertThat(contentType)
                .as("Response Content-Type should be application/json for WHATSAPP")
                .contains("application/json");

        String responseBody = result.getResponse().getContentAsString();
        var jsonNode = objectMapper.readTree(responseBody);
        assertThat(jsonNode.get("type").asText()).isEqualTo("WHATSAPP");
    }

    @Property
    @DisplayName("Property: All valid notification requests should return application/json response")
    void allValidNotificationsReturnJsonResponse(
            @ForAll @NotEmpty @StringLength(min = 1, max = 50) String recipient,
            @ForAll @NotEmpty @StringLength(min = 1, max = 100) String message) throws Exception {

        String validRequest = """
                {
                  "type": "EMAIL",
                  "recipient": "%s",
                  "message": "%s"
                }
                """.formatted(recipient.replace("\"", "\\\""), message.replace("\"", "\\\""));

        MvcResult result = mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest))
                .andExpect(status().isOk())
                .andReturn();

        String contentType = result.getResponse().getContentType();
        assertThat(contentType)
                .as("All valid requests should return application/json Content-Type")
                .contains("application/json");

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody)
                .as("Response body should be valid JSON")
                .startsWith("{")
                .endsWith("}");

        var jsonNode = objectMapper.readTree(responseBody);
        assertThat(jsonNode.has("id")).isTrue();
        assertThat(jsonNode.has("type")).isTrue();
        assertThat(jsonNode.has("status")).isTrue();
    }
}
