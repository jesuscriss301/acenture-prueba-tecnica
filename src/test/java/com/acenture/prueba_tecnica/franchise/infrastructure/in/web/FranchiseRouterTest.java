package com.acenture.prueba_tecnica.franchise.infrastructure.in.web;

import com.acenture.prueba_tecnica.franchise.application.port.in.CreateFranchiseUseCase.CreateFranchiseCommand;
import com.acenture.prueba_tecnica.franchise.application.port.in.CreateFranchiseUseCase;
import com.acenture.prueba_tecnica.franchise.application.port.in.RenameFranchiseUseCase;
import com.acenture.prueba_tecnica.franchise.domain.model.Franchise;
import com.acenture.prueba_tecnica.shared.domain.exception.DomainValidationException;
import com.acenture.prueba_tecnica.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FranchiseRouterTest {

    private CreateFranchiseUseCase createFranchiseUseCase;
    private RenameFranchiseUseCase renameFranchiseUseCase;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        createFranchiseUseCase = mock(CreateFranchiseUseCase.class);
        renameFranchiseUseCase = mock(RenameFranchiseUseCase.class);
        FranchiseHandler handler = new FranchiseHandler(createFranchiseUseCase, renameFranchiseUseCase);
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

    @Test
    void patchNameShouldReturn200() {
        when(renameFranchiseUseCase.rename(any()))
                .thenReturn(Mono.just(new Franchise("abc-123", "Franquicia Sur")));

        client.patch().uri(FranchiseRouter.BASE_PATH + "/abc-123/name")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Franquicia Sur\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Franquicia Sur");
    }

    @Test
    void patchNameShouldReturn404WhenFranchiseDoesNotExist() {
        when(renameFranchiseUseCase.rename(any()))
                .thenReturn(Mono.error(new ResourceNotFoundException("La franquicia f-x no existe")));

        client.patch().uri(FranchiseRouter.BASE_PATH + "/f-x/name")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Franquicia Sur\"}")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void postShouldReturn400WhenBodyIsEmpty() {
        client.post().uri(FranchiseRouter.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("El cuerpo de la petición es obligatorio");
    }

    @Test
    void postShouldSendNameToUseCase() {
        when(createFranchiseUseCase.create(any()))
                .thenReturn(Mono.just(new Franchise("abc-123", "Franquicia Norte")));

        client.post().uri(FranchiseRouter.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Franquicia Norte\"}")
                .exchange()
                .expectStatus().isCreated();

        verify(createFranchiseUseCase).create(new CreateFranchiseCommand("Franquicia Norte"));
    }

    @Test
    void patchNameShouldReturn400WhenNameIsBlank() {
        when(renameFranchiseUseCase.rename(any()))
                .thenReturn(Mono.error(new DomainValidationException("El nombre de la franquicia es obligatorio")));

        client.patch().uri(FranchiseRouter.BASE_PATH + "/abc-123/name")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"\"}")
                .exchange()
                .expectStatus().isBadRequest();
    }
}
