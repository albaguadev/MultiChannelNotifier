package com.notifier.controller;

import com.notifier.exception.PersistenceException;
import com.notifier.service.QueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link QueryController} exception handling behavior.
 * <p>
 * This test suite verifies that persistence failures during query operations
 * are properly handled by the {@link com.notifier.exception.GlobalExceptionHandler}
 * and result in appropriate HTTP responses.
 * </p>
 * <p>
 * Validates requirement: 3.6 - "IF el NotificationRepository no puede recuperar
 * los registros por un fallo de infraestructura, THEN THE QueryService SHALL
 * propagar una excepción tipificada que permita al llamador distinguir el fallo
 * de persistencia de un resultado vacío."
 * </p>
 */
@SpringBootTest
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@AutoConfigureMockMvc
class QueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QueryService queryService;

    /**
     * Set up the MockMvc instance with REST Docs configuration before each test.
     *
     * @param webApplicationContext The Spring application context.
     * @param restDocumentation The REST Docs provider.
     */
    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .addDispatcherServletCustomizer(ds -> ds.setThrowExceptionIfNoHandlerFound(true))
                .build();
    }

    /**
     * Test: QueryController returns HTTP 400 when an invalid {@code type} parameter is provided.
     * <p>
     * Given: A GET request with an unrecognized value for the {@code type} query parameter<br>
     * When: {@code GET /api/v1/notifications?type=INVALIDO} is invoked<br>
     * Then: The response status is 400 Bad Request<br>
     * And: The response body contains a descriptive message indicating the unrecognized value
     * </p>
     * <p>
     * This test verifies Requirement 4.5: unrecognized values for {@code type} or {@code status}
     * must result in an HTTP 400 response with a descriptive error message.
     * </p>
     */
    @Test
    @DisplayName("QueryController retorna HTTP 400 con parámetro type inválido")
    void queryController_invalidTypeParameter_returnsHttp400() throws Exception {
        // When: A GET request is made with an unrecognized type value
        // Then: The response status is 400 Bad Request
        // And: The response body contains a descriptive message about the unrecognized value
        mockMvc.perform(get("/api/v1/notifications").param("type", "INVALIDO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Valor no reconocido para el parámetro 'type': INVALIDO"));
    }

    /**
     * Test: QueryController returns HTTP 200 with an empty JSON array when no records exist.
     * <p>
     * Given: A QueryService that returns an empty list when queried<br>
     * When: A GET request is made to /api/v1/notifications<br>
     * Then: The response status is 200 OK<br>
     * And: The response body is an empty JSON array {@code []}
     * </p>
     * <p>
     * This test verifies Requirement 4.6: when no records exist, the endpoint must
     * return HTTP 200 with an empty list rather than a 404 or error response.
     * </p>
     */
    @Test
    @DisplayName("QueryController retorna HTTP 200 con lista vacía cuando no hay registros")
    void queryController_noRecordsExist_returnsHttp200WithEmptyList() throws Exception {
        // Given: The QueryService returns an empty list
        when(queryService.query(any(), any(), any(), any())).thenReturn(Collections.emptyList());

        // When: A GET request is made to /api/v1/notifications
        // Then: The response status is 200 OK
        // And: The response body is an empty JSON array
        mockMvc.perform(get("/api/v1/notifications")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

    /**
     * Test: GlobalExceptionHandler handles PersistenceException and returns HTTP 503.
     * <p>
     * Given: A QueryService that throws a PersistenceException when query is called<br>
     * When: A GET request is made to /api/v1/notifications<br>
     * Then: The response status is 503 Service Unavailable<br>
     * And: The response body contains the message "Error al recuperar los registros de notificación"
     * </p>
     * <p>
     * This test verifies Requirement 3.6: infrastructure failures during read operations
     * must result in an HTTP 503 response with a descriptive error message.
     * </p>
     */
    @Test
    @DisplayName("GlobalExceptionHandler maneja PersistenceException → HTTP 503")
    void globalExceptionHandler_handlesPersistenceException_returnsHttp503() throws Exception {
        // Given: The QueryService throws a PersistenceException when query is called
        when(queryService.query(any(), any(), any(), any())).thenThrow(
                new PersistenceException(
                        "Failed to retrieve notification records",
                        new RuntimeException("Database connection timeout")
                )
        );

        // When: A GET request is made to /api/v1/notifications
        // Then: The response status is 503 Service Unavailable
        // And: The response body contains the expected error message
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("Error al recuperar los registros de notificación"));
    }
}
