package com.acenture.prueba_tecnica.branch.infrastructure.in.web;

import com.acenture.prueba_tecnica.branch.application.port.in.AddBranchUseCase;
import com.acenture.prueba_tecnica.branch.application.port.in.RenameBranchUseCase;
import com.acenture.prueba_tecnica.branch.domain.model.Branch;
import com.acenture.prueba_tecnica.shared.domain.exception.DomainValidationException;
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
    private RenameBranchUseCase renameBranchUseCase;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        addBranchUseCase = mock(AddBranchUseCase.class);
        renameBranchUseCase = mock(RenameBranchUseCase.class);
        BranchHandler handler = new BranchHandler(addBranchUseCase, renameBranchUseCase);
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

    @Test
    void patchNameShouldReturn200() {
        when(renameBranchUseCase.rename(any()))
                .thenReturn(Mono.just(new Branch("b-1", "f-1", "Sucursal Sur")));

        client.patch().uri("/api/franchises/f-1/branches/b-1/name")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Sucursal Sur\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Sucursal Sur");
    }

    @Test
    void postShouldReturn400WhenNameIsBlank() {
        when(addBranchUseCase.add(any()))
                .thenReturn(Mono.error(new DomainValidationException("El nombre de la sucursal es obligatorio")));

        client.post().uri("/api/franchises/f-1/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("El nombre de la sucursal es obligatorio");
    }

    @Test
    void postShouldReturn400WhenJsonIsInvalid() {
        client.post().uri("/api/franchises/f-1/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{ no es json")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void patchNameShouldReturn404WhenBranchDoesNotExist() {
        when(renameBranchUseCase.rename(any()))
                .thenReturn(Mono.error(new ResourceNotFoundException("La sucursal b-x no existe en la franquicia f-1")));

        client.patch().uri("/api/franchises/f-1/branches/b-x/name")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Sucursal Sur\"}")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void patchNameShouldReturn400WhenNameIsBlank() {
        when(renameBranchUseCase.rename(any()))
                .thenReturn(Mono.error(new DomainValidationException("El nombre de la sucursal es obligatorio")));

        client.patch().uri("/api/franchises/f-1/branches/b-1/name")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"\"}")
                .exchange()
                .expectStatus().isBadRequest();
    }
}
