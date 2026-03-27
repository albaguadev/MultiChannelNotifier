package com.notifier.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    @DisplayName("Test: Successful Notification Delivery (200 OK)")
    void shouldSendNotificationSuccessfully() throws Exception {
        String validRequest = """
                {
                  "type": "EMAIL",
                  "recipient": "albaguadev@example.com",
                  "message": "Mensaje verificado por suite de QA"
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
    @DisplayName("Test: Validation Failure for Missing Fields (400 Bad Request)")
    void shouldReturnBadRequestForEmptyFields() throws Exception {
        String invalidRequest = """
                {
                  "type": "EMAIL",
                  "recipient": "",
                  "message": ""
                }
                """;

        this.mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andDo(document("notification-error-validation",
                        preprocessResponse(prettyPrint())
                ));
    }
}