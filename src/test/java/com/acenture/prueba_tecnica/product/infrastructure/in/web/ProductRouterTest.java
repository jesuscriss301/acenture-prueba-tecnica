package com.acenture.prueba_tecnica.product.infrastructure.in.web;

import com.acenture.prueba_tecnica.product.application.port.in.AddProductUseCase;
import com.acenture.prueba_tecnica.product.application.port.in.DeleteProductUseCase.DeleteProductCommand;
import com.acenture.prueba_tecnica.product.application.port.in.DeleteProductUseCase;
import com.acenture.prueba_tecnica.product.application.port.in.RenameProductUseCase;
import com.acenture.prueba_tecnica.product.application.port.in.UpdateProductStockUseCase.UpdateProductStockCommand;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductRouterTest {

    private static final String PRODUCTS_URI = "/api/franchises/f-1/branches/b-1/products";

    private AddProductUseCase addProductUseCase;
    private DeleteProductUseCase deleteProductUseCase;
    private UpdateProductStockUseCase updateProductStockUseCase;
    private RenameProductUseCase renameProductUseCase;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        addProductUseCase = mock(AddProductUseCase.class);
        deleteProductUseCase = mock(DeleteProductUseCase.class);
        updateProductStockUseCase = mock(UpdateProductStockUseCase.class);
        renameProductUseCase = mock(RenameProductUseCase.class);
        ProductHandler handler = new ProductHandler(
                addProductUseCase, deleteProductUseCase, updateProductStockUseCase, renameProductUseCase);
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

    @Test
    void patchNameShouldReturn200() {
        when(renameProductUseCase.rename(any()))
                .thenReturn(Mono.just(new Product("p-1", "f-1", "b-1", "Café Especial", 5)));

        client.patch().uri(PRODUCTS_URI + "/p-1/name")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Café Especial\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Café Especial")
                .jsonPath("$.stock").isEqualTo(5);
    }

    @Test
    void postShouldReturn404WhenBranchDoesNotExist() {
        when(addProductUseCase.add(any()))
                .thenReturn(Mono.error(new ResourceNotFoundException("La sucursal b-1 no existe en la franquicia f-1")));

        client.post().uri(PRODUCTS_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Café\",\"stock\":5}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("NOT_FOUND");
    }

    @Test
    void postShouldReturn400WhenStockIsNotANumber() {
        client.post().uri(PRODUCTS_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Café\",\"stock\":\"muchos\"}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void deleteShouldSendPathVariablesToUseCase() {
        when(deleteProductUseCase.delete(any())).thenReturn(Mono.empty());

        client.delete().uri(PRODUCTS_URI + "/p-9")
                .exchange()
                .expectStatus().isNoContent();

        verify(deleteProductUseCase).delete(new DeleteProductCommand("f-1", "b-1", "p-9"));
    }

    @Test
    void deleteShouldReturnErrorBodyWhenProductDoesNotExist() {
        when(deleteProductUseCase.delete(any()))
                .thenReturn(Mono.error(new ResourceNotFoundException("El producto p-x no existe en la sucursal b-1")));

        client.delete().uri(PRODUCTS_URI + "/p-x")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("El producto p-x no existe en la sucursal b-1");
    }

    @Test
    void patchStockShouldReturn404WhenProductDoesNotExist() {
        when(updateProductStockUseCase.updateStock(any()))
                .thenReturn(Mono.error(new ResourceNotFoundException("El producto p-x no existe en la sucursal b-1")));

        client.patch().uri(PRODUCTS_URI + "/p-x/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":10}")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void patchStockShouldReturn400WhenStockIsNegative() {
        when(updateProductStockUseCase.updateStock(any()))
                .thenReturn(Mono.error(new DomainValidationException("El stock no puede ser negativo")));

        client.patch().uri(PRODUCTS_URI + "/p-1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":-1}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("El stock no puede ser negativo");
    }

    @Test
    void patchStockShouldSendValuesToUseCase() {
        when(updateProductStockUseCase.updateStock(any()))
                .thenReturn(Mono.just(new Product("p-1", "f-1", "b-1", "Café", 15)));

        client.patch().uri(PRODUCTS_URI + "/p-1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":15}")
                .exchange()
                .expectStatus().isOk();

        verify(updateProductStockUseCase).updateStock(new UpdateProductStockCommand("f-1", "b-1", "p-1", 15));
    }

    @Test
    void patchNameShouldReturn404WhenProductDoesNotExist() {
        when(renameProductUseCase.rename(any()))
                .thenReturn(Mono.error(new ResourceNotFoundException("El producto p-x no existe en la sucursal b-1")));

        client.patch().uri(PRODUCTS_URI + "/p-x/name")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Nuevo\"}")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void patchNameShouldReturn400WhenNameIsBlank() {
        when(renameProductUseCase.rename(any()))
                .thenReturn(Mono.error(new DomainValidationException("El nombre del producto es obligatorio")));

        client.patch().uri(PRODUCTS_URI + "/p-1/name")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"\"}")
                .exchange()
                .expectStatus().isBadRequest();
    }
}
