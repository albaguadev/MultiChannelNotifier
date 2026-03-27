package com.notifier.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.stream.Stream;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Technical documentation and integration testing for the Notification API.
 * This class ensures that the API contract is validated and documented
 * using Spring REST Docs.
 * * @author albaguadev
 */
@SpringBootTest
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@AutoConfigureMockMvc
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Set up the MockMvc instance with REST Docs configuration before each test.
     * * @param webApplicationContext The Spring application context.
     * @param restDocumentation The REST Docs provider.
     */
    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    @DisplayName("Execution: Successful dispatch without optional fields - Response 200 OK")
    void shouldSendNotificationSuccessfullyWithoutOptionalFields() throws Exception {
        String validRequest = """
                {
                  "type": "EMAIL",
                  "recipient": "albaguadev@example.com",
                  "message": "Automated QA verification message - System Integrity Test"                
                }
                """;

        this.mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest))
                .andExpect(status().isOk())
                .andDo(document("notification-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    @DisplayName("Execution: Successful dispatch with optional fields - Response 200 OK")
    void shouldSendNotificationWithOptionalFields() throws Exception {
        String requestWithOptional = """
            {
              "type": "EMAIL",
              "recipient": "albaguadev@example.com",
              "message": "Automated QA verification message",
              "subject": "Test QA"
            }
            """;

        this.mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestWithOptional))
                .andExpect(status().isOk())
                .andDo(document("notification-success-optional",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidRequests")
    @DisplayName("Test: Validation Failure for partial/total Missing Fields (400 Bad Request)")
    void shouldValidateDifferentInvalidInputs(String jsonBody, String docName) throws Exception {
        this.mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andDo(document("notification-error-validation-" + docName,
                        preprocessResponse(prettyPrint())
                ));
    }

    private static Stream<Arguments> provideInvalidRequests() {
        return Stream.of(
                Arguments.of("""
        {
          "type": null,
          "recipient": "albaguadev@example.com",
          "message": "System Integrity Test",
          "subject": "QA Verification"
        }
        """, "null-type"),

                Arguments.of("""
        {
          "type": "",
          "recipient": "albaguadev@example.com",
          "message": "System Integrity Test",
          "subject": "QA Verification"
        }
        """, "empty-type"),

                Arguments.of("""
        {
          "type": "EMAIL",
          "recipient": "",
          "message": "System Integrity Test",
          "subject": "QA Verification"
        }
        """, "empty-recipient"),

                Arguments.of("""
        {
          "type": "EMAIL",
          "recipient": null,
          "message": "System Integrity Test",
          "subject": "QA Verification"
        }
        """, "null-recipient"),

                Arguments.of("""
        {
          "type": "EMAIL",
          "recipient": "albaguadev@example.com",
          "message": "",
          "subject": "QA Verification"
        }
        """, "empty-message"),

                Arguments.of("""
        {
          "type": "EMAIL",
          "recipient": "albaguadev@example.com",
          "message": null,
          "subject": "QA Verification"
        }
        """, "null-message"),

                Arguments.of("""
        {
          "type": "",
          "recipient": "",
          "message": "",
          "subject": "QA Verification"
        }
        """, "empty-fields-body"),

                Arguments.of("""
        {
          "type": null,
          "recipient": null,
          "message": null,
          "subject": "QA Verification"
        }
        """, "null-fields-body")
        );
    }

    @Test
    @DisplayName("Execution: Invalid Email Format - Response 400 Bad Request")
    void shouldReturnErrorForMalformedEmail() throws Exception {
        String malformedRequest = """
        {
          "type": "EMAIL",
          "recipient": "not-an-email-format",
          "message": "System Integrity Test"
        }
        """;

        this.mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedRequest))
                .andExpect(status().isBadRequest())
                .andDo(document("notification-error-email-format",
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    @DisplayName("Execution: Incorrect JSON Keys - Response 400 Bad Request")
    void shouldReturnErrorForInvalidJsonKeys() throws Exception {
        String invalidKeysRequest = """
        {
          "notification_type": "EMAIL",
          "user_address": "albaguadev@example.com",
          "body_content": "This keys do not match the DTO"
        }
        """;

        this.mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidKeysRequest))
                .andExpect(status().isBadRequest())
                .andDo(document("notification-error-invalid-keys",
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    @DisplayName("Execution: Resource Not Found - Response 404")
    void shouldReturnNotFoundForInvalidPath() throws Exception {
        this.mockMvc.perform(post("/api/v1/non-existent-endpoint")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andDo(document("notification-error-404",
                        preprocessResponse(prettyPrint())
                ));
    }
}