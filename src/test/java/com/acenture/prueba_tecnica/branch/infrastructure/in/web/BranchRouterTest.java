package com.acenture.prueba_tecnica.branch.infrastructure.in.web;

import com.acenture.prueba_tecnica.branch.application.port.in.AddBranchUseCase;
import com.acenture.prueba_tecnica.branch.domain.model.Branch;
import com.acenture.prueba_tecnica.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BranchRouterTest {

    private AddBranchUseCase addBranchUseCase;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        addBranchUseCase = mock(AddBranchUseCase.class);
        BranchHandler handler = new BranchHandler(addBranchUseCase);
        client = WebTestClient.bindToRouterFunction(new BranchRouter().branchRoutes(handler)).build();
    }

    @Test
    void postShouldReturn201() {
        when(addBranchUseCase.add(any())).thenReturn(Mono.just(new Branch("b-1", "f-1", "Sucursal Centro")));

        client.post().uri("/api/franchises/f-1/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Sucursal Centro\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location("/api/franchises/f-1/branches/b-1")
                .expectBody()
                .jsonPath("$.id").isEqualTo("b-1")
                .jsonPath("$.franchiseId").isEqualTo("f-1")
                .jsonPath("$.name").isEqualTo("Sucursal Centro");
    }

    @Test
    void postShouldReturn404WhenFranchiseDoesNotExist() {
        when(addBranchUseCase.add(any()))
                .thenReturn(Mono.error(new ResourceNotFoundException("La franquicia f-x no existe")));

        client.post().uri("/api/franchises/f-x/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Sucursal Centro\"}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("NOT_FOUND");
    }
}
