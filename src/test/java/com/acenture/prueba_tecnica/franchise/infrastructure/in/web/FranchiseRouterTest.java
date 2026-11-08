package com.acenture.prueba_tecnica.franchise.infrastructure.in.web;

import com.acenture.prueba_tecnica.franchise.application.port.in.CreateFranchiseUseCase;
import com.acenture.prueba_tecnica.franchise.domain.model.Franchise;
import com.acenture.prueba_tecnica.shared.domain.exception.DomainValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FranchiseRouterTest {

    private CreateFranchiseUseCase createFranchiseUseCase;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        createFranchiseUseCase = mock(CreateFranchiseUseCase.class);
        FranchiseHandler handler = new FranchiseHandler(createFranchiseUseCase);
        client = WebTestClient.bindToRouterFunction(new FranchiseRouter().franchiseRoutes(handler)).build();
    }

    @Test
    void postShouldReturn201WithLocationAndBody() {
        when(createFranchiseUseCase.create(any()))
                .thenReturn(Mono.just(new Franchise("abc-123", "Franquicia Norte")));

        client.post().uri(FranchiseRouter.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Franquicia Norte\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location(FranchiseRouter.BASE_PATH + "/abc-123")
                .expectBody()
                .jsonPath("$.id").isEqualTo("abc-123")
                .jsonPath("$.name").isEqualTo("Franquicia Norte");
    }

    @Test
    void postShouldReturn400WhenDomainValidationFails() {
        when(createFranchiseUseCase.create(any()))
                .thenReturn(Mono.error(new DomainValidationException("El nombre de la franquicia es obligatorio")));

        client.post().uri(FranchiseRouter.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("BAD_REQUEST")
                .jsonPath("$.message").isEqualTo("El nombre de la franquicia es obligatorio");
    }

    @Test
    void postShouldReturn400WhenJsonIsInvalid() {
        client.post().uri(FranchiseRouter.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{ esto no es json")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("BAD_REQUEST");
    }
}
