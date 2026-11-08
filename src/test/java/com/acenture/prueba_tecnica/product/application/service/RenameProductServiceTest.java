package com.acenture.prueba_tecnica.product.application.service;

import com.acenture.prueba_tecnica.product.application.port.in.RenameProductUseCase.RenameProductCommand;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
}
