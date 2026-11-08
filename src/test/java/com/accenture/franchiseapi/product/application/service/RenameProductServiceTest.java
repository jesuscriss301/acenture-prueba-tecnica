package com.accenture.franchiseapi.product.application.service;

import com.accenture.franchiseapi.product.application.port.in.RenameProductUseCase.RenameProductCommand;
import com.accenture.franchiseapi.product.application.port.out.ProductRepositoryPort;
import com.accenture.franchiseapi.product.domain.model.Product;
import com.accenture.franchiseapi.shared.domain.exception.DomainValidationException;
import com.accenture.franchiseapi.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RenameProductServiceTest {

    @Mock
    private ProductRepositoryPort productRepository;

    private RenameProductService service;

    @BeforeEach
    void setUp() {
        service = new RenameProductService(productRepository);
    }

    @Test
    void shouldRenameProductKeepingStock() {
        when(productRepository.findById("f-1", "b-1", "p-1"))
                .thenReturn(Mono.just(new Product("p-1", "f-1", "b-1", "Café", 8)));
        when(productRepository.update(any(Product.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.rename(new RenameProductCommand("f-1", "b-1", "p-1", "Café Especial")))
                .assertNext(product -> {
                    assertThat(product.name()).isEqualTo("Café Especial");
                    assertThat(product.stock()).isEqualTo(8);
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenProductDoesNotExist() {
        when(productRepository.findById("f-1", "b-1", "p-x")).thenReturn(Mono.empty());

        StepVerifier.create(service.rename(new RenameProductCommand("f-1", "b-1", "p-x", "Nuevo")))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldRejectBlankNameAndNotUpdate() {
        when(productRepository.findById("f-1", "b-1", "p-1"))
                .thenReturn(Mono.just(new Product("p-1", "f-1", "b-1", "Café", 8)));

        StepVerifier.create(service.rename(new RenameProductCommand("f-1", "b-1", "p-1", " ")))
                .expectError(DomainValidationException.class)
                .verify();

        verify(productRepository, never()).update(any());
    }

    @Test
    void shouldRejectNameLongerThanMax() {
        when(productRepository.findById("f-1", "b-1", "p-1"))
                .thenReturn(Mono.just(new Product("p-1", "f-1", "b-1", "Café", 8)));

        StepVerifier.create(service.rename(new RenameProductCommand("f-1", "b-1", "p-1", "x".repeat(101))))
                .expectError(DomainValidationException.class)
                .verify();
    }

    @Test
    void shouldPropagateRepositoryError() {
        when(productRepository.findById("f-1", "b-1", "p-1"))
                .thenReturn(Mono.just(new Product("p-1", "f-1", "b-1", "Café", 8)));
        when(productRepository.update(any(Product.class)))
                .thenReturn(Mono.error(new IllegalStateException("DynamoDB no disponible")));

        StepVerifier.create(service.rename(new RenameProductCommand("f-1", "b-1", "p-1", "Nuevo")))
                .expectErrorMessage("DynamoDB no disponible")
                .verify();
    }
}
