package com.acenture.prueba_tecnica.product.infrastructure.in.web;

import com.acenture.prueba_tecnica.product.application.port.in.AddProductUseCase;
import com.acenture.prueba_tecnica.product.application.port.in.DeleteProductUseCase;
import com.acenture.prueba_tecnica.product.application.port.in.UpdateProductStockUseCase;
import com.acenture.prueba_tecnica.product.domain.model.Product;
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

class ProductRouterTest {

    private static final String PRODUCTS_URI = "/api/franchises/f-1/branches/b-1/products";

    private AddProductUseCase addProductUseCase;
    private DeleteProductUseCase deleteProductUseCase;
    private UpdateProductStockUseCase updateProductStockUseCase;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        addProductUseCase = mock(AddProductUseCase.class);
        deleteProductUseCase = mock(DeleteProductUseCase.class);
        updateProductStockUseCase = mock(UpdateProductStockUseCase.class);
        ProductHandler handler = new ProductHandler(
                addProductUseCase, deleteProductUseCase, updateProductStockUseCase);
        client = WebTestClient.bindToRouterFunction(new ProductRouter().productRoutes(handler)).build();
    }

    @Test
    void postShouldReturn201() {
        when(addProductUseCase.add(any())).thenReturn(Mono.just(new Product("p-1", "f-1", "b-1", "Café", 5)));

        client.post().uri(PRODUCTS_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Café\",\"stock\":5}")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location(PRODUCTS_URI + "/p-1")
                .expectBody()
                .jsonPath("$.id").isEqualTo("p-1")
                .jsonPath("$.stock").isEqualTo(5);
    }

    @Test
    void postShouldReturn400WhenStockIsNegative() {
        when(addProductUseCase.add(any()))
                .thenReturn(Mono.error(new DomainValidationException("El stock no puede ser negativo")));

        client.post().uri(PRODUCTS_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Café\",\"stock\":-1}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("El stock no puede ser negativo");
    }

    @Test
    void deleteShouldReturn204() {
        when(deleteProductUseCase.delete(any())).thenReturn(Mono.empty());

        client.delete().uri(PRODUCTS_URI + "/p-1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteShouldReturn404WhenProductDoesNotExist() {
        when(deleteProductUseCase.delete(any()))
                .thenReturn(Mono.error(new ResourceNotFoundException("El producto p-x no existe en la sucursal b-1")));

        client.delete().uri(PRODUCTS_URI + "/p-x")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void patchStockShouldReturn200WithUpdatedProduct() {
        when(updateProductStockUseCase.updateStock(any()))
                .thenReturn(Mono.just(new Product("p-1", "f-1", "b-1", "Café", 42)));

        client.patch().uri(PRODUCTS_URI + "/p-1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":42}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.stock").isEqualTo(42);
    }
}
