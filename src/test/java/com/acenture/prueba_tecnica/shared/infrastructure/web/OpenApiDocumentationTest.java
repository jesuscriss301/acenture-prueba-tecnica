package com.acenture.prueba_tecnica.shared.infrastructure.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica, con la aplicación levantada, que la documentación se sirve
 * y que Swagger UI usa el contrato escrito a mano (static/openapi.yaml).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiDocumentationTest {

    @Value("${local.server.port}")
    private int port;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void openApiContractIsServed() {
        client.get().uri("/openapi.yaml")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).startsWith("openapi: 3.0.3");
                    assertThat(body).contains("/api/franchises/{franchiseId}/top-stock-products");
                });
    }

    @Test
    void swaggerUiPointsToTheContract() {
        client.get().uri("/v3/api-docs/swagger-config")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("/openapi.yaml"));
    }

    @Test
    void swaggerUiIsAvailable() {
        client.get().uri("/swagger-ui.html")
                .exchange()
                .expectStatus().value(status -> assertThat(status).isBetween(200, 399));
    }
}
