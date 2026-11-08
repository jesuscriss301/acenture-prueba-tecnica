package com.accenture.franchiseapi.product.infrastructure.in.web;

import com.accenture.franchiseapi.product.application.port.in.GetTopStockProductsUseCase;
import com.accenture.franchiseapi.product.domain.model.BranchTopStockProduct;
import com.accenture.franchiseapi.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TopStockProductRouterTest {

    private GetTopStockProductsUseCase useCase;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        useCase = mock(GetTopStockProductsUseCase.class);
        TopStockProductHandler handler = new TopStockProductHandler(useCase);
        client = WebTestClient.bindToRouterFunction(new ProductRouter().topStockProductRoutes(handler)).build();
    }

    @Test
    void getShouldReturnListWithBranchOfEachProduct() {
        when(useCase.getByFranchise("f-1")).thenReturn(Flux.just(
                new BranchTopStockProduct("b-1", "Sucursal Centro", "p-2", "Té", 30)));

        client.get().uri("/api/franchises/f-1/top-stock-products")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].branchName").isEqualTo("Sucursal Centro")
                .jsonPath("$[0].productName").isEqualTo("Té")
                .jsonPath("$[0].stock").isEqualTo(30);
    }

    @Test
    void getShouldReturn404WhenFranchiseDoesNotExist() {
        when(useCase.getByFranchise("f-x"))
                .thenReturn(Flux.error(new ResourceNotFoundException("La franquicia f-x no existe")));

        client.get().uri("/api/franchises/f-x/top-stock-products")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getShouldReturnEmptyArrayWhenThereAreNoProducts() {
        when(useCase.getByFranchise("f-1")).thenReturn(Flux.empty());

        client.get().uri("/api/franchises/f-1/top-stock-products")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json("[]");
    }

    @Test
    void getShouldKeepOrderReturnedByUseCase() {
        when(useCase.getByFranchise("f-1")).thenReturn(Flux.just(
                new BranchTopStockProduct("b-1", "Sucursal Centro", "p-2", "Té", 30),
                new BranchTopStockProduct("b-2", "Sucursal Norte", "p-3", "Pan", 7)));

        client.get().uri("/api/franchises/f-1/top-stock-products")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].branchId").isEqualTo("b-1")
                .jsonPath("$[1].branchId").isEqualTo("b-2")
                .jsonPath("$[1].productId").isEqualTo("p-3");
    }

    @Test
    void getShouldReturnErrorBodyWhenFranchiseDoesNotExist() {
        when(useCase.getByFranchise("f-x"))
                .thenReturn(Flux.error(new ResourceNotFoundException("La franquicia f-x no existe")));

        client.get().uri("/api/franchises/f-x/top-stock-products")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("La franquicia f-x no existe");
    }
}
