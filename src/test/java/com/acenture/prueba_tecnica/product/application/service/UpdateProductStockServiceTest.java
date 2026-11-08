package com.acenture.prueba_tecnica.product.application.service;

import com.acenture.prueba_tecnica.product.application.port.in.UpdateProductStockUseCase.UpdateProductStockCommand;
import com.acenture.prueba_tecnica.product.application.port.out.ProductRepositoryPort;
import com.acenture.prueba_tecnica.product.domain.model.Product;
import com.acenture.prueba_tecnica.shared.domain.exception.DomainValidationException;
import com.acenture.prueba_tecnica.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateProductStockServiceTest {

    @Mock
    private ProductRepositoryPort productRepository;

    private UpdateProductStockService service;

    @BeforeEach
    void setUp() {
        service = new UpdateProductStockService(productRepository);
    }

    @Test
    void shouldUpdateStock() {
        when(productRepository.findById("f-1", "b-1", "p-1"))
                .thenReturn(Mono.just(new Product("p-1", "f-1", "b-1", "Café", 5)));
        when(productRepository.update(any(Product.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.updateStock(new UpdateProductStockCommand("f-1", "b-1", "p-1", 20)))
                .assertNext(product -> assertThat(product.stock()).isEqualTo(20))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenProductDoesNotExist() {
        when(productRepository.findById("f-1", "b-1", "p-x")).thenReturn(Mono.empty());

        StepVerifier.create(service.updateStock(new UpdateProductStockCommand("f-1", "b-1", "p-x", 20)))
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(productRepository, never()).update(any());
    }

    @Test
    void shouldRejectNegativeStockWithoutQuerying() {
        StepVerifier.create(service.updateStock(new UpdateProductStockCommand("f-1", "b-1", "p-1", -3)))
                .expectError(DomainValidationException.class)
                .verify();

        verify(productRepository, never()).findById(anyString(), anyString(), anyString());
    }

    @Test
    void shouldRejectMissingStock() {
        StepVerifier.create(service.updateStock(new UpdateProductStockCommand("f-1", "b-1", "p-1", null)))
                .expectErrorMessage("El stock es obligatorio")
                .verify();

        verify(productRepository, never()).findById(anyString(), anyString(), anyString());
    }

    @Test
    void shouldAllowZeroStock() {
        when(productRepository.findById("f-1", "b-1", "p-1"))
                .thenReturn(Mono.just(new Product("p-1", "f-1", "b-1", "Café", 5)));
        when(productRepository.update(any(Product.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.updateStock(new UpdateProductStockCommand("f-1", "b-1", "p-1", 0)))
                .assertNext(product -> assertThat(product.stock()).isZero())
                .verifyComplete();
    }

    @Test
    void shouldKeepNameWhenUpdatingStock() {
        when(productRepository.findById("f-1", "b-1", "p-1"))
                .thenReturn(Mono.just(new Product("p-1", "f-1", "b-1", "Café", 5)));
        when(productRepository.update(any(Product.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.updateStock(new UpdateProductStockCommand("f-1", "b-1", "p-1", 9)))
                .assertNext(product -> assertThat(product.name()).isEqualTo("Café"))
                .verifyComplete();
    }
}
