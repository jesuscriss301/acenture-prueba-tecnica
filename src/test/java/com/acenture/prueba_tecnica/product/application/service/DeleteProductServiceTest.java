package com.acenture.prueba_tecnica.product.application.service;

import com.acenture.prueba_tecnica.product.application.port.in.DeleteProductUseCase.DeleteProductCommand;
import com.acenture.prueba_tecnica.product.application.port.out.ProductRepositoryPort;
import com.acenture.prueba_tecnica.product.domain.model.Product;
import com.acenture.prueba_tecnica.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteProductServiceTest {

    @Mock
    private ProductRepositoryPort productRepository;

    private DeleteProductService service;

    @BeforeEach
    void setUp() {
        service = new DeleteProductService(productRepository);
    }

    @Test
    void shouldCompleteWhenProductWasDeleted() {
        when(productRepository.deleteById("f-1", "b-1", "p-1"))
                .thenReturn(Mono.just(new Product("p-1", "f-1", "b-1", "Café", 5)));

        StepVerifier.create(service.delete(new DeleteProductCommand("f-1", "b-1", "p-1")))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenProductDoesNotExist() {
        when(productRepository.deleteById("f-1", "b-1", "p-x")).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(new DeleteProductCommand("f-1", "b-1", "p-x")))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldPropagateRepositoryError() {
        when(productRepository.deleteById("f-1", "b-1", "p-1"))
                .thenReturn(Mono.error(new IllegalStateException("DynamoDB no disponible")));

        StepVerifier.create(service.delete(new DeleteProductCommand("f-1", "b-1", "p-1")))
                .expectErrorMessage("DynamoDB no disponible")
                .verify();
    }

    @Test
    void shouldCallRepositoryWithAllIds() {
        when(productRepository.deleteById("f-1", "b-1", "p-1"))
                .thenReturn(Mono.just(new Product("p-1", "f-1", "b-1", "Café", 5)));

        StepVerifier.create(service.delete(new DeleteProductCommand("f-1", "b-1", "p-1")))
                .verifyComplete();

        verify(productRepository).deleteById("f-1", "b-1", "p-1");
    }

    @Test
    void notFoundMessageShouldIdentifyProductAndBranch() {
        when(productRepository.deleteById("f-1", "b-1", "p-x")).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(new DeleteProductCommand("f-1", "b-1", "p-x")))
                .expectErrorMessage("El producto p-x no existe en la sucursal b-1")
                .verify();
    }
}
