package com.accenture.franchiseapi.product.application.service;

import com.accenture.franchiseapi.branch.application.port.out.BranchRepositoryPort;
import com.accenture.franchiseapi.product.application.port.in.AddProductUseCase.AddProductCommand;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddProductServiceTest {

    @Mock
    private BranchRepositoryPort branchRepository;
    @Mock
    private ProductRepositoryPort productRepository;

    private AddProductService service;

    @BeforeEach
    void setUp() {
        service = new AddProductService(branchRepository, productRepository);
    }

    @Test
    void shouldAddProductWhenBranchExists() {
        when(branchRepository.existsById("f-1", "b-1")).thenReturn(Mono.just(true));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.add(new AddProductCommand("f-1", "b-1", "Café", 5)))
                .assertNext(product -> {
                    assertThat(product.branchId()).isEqualTo("b-1");
                    assertThat(product.stock()).isEqualTo(5);
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenBranchDoesNotExist() {
        when(branchRepository.existsById("f-1", "b-x")).thenReturn(Mono.just(false));

        StepVerifier.create(service.add(new AddProductCommand("f-1", "b-x", "Café", 5)))
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldRejectNegativeStockWithoutQuerying() {
        StepVerifier.create(service.add(new AddProductCommand("f-1", "b-1", "Café", -5)))
                .expectError(DomainValidationException.class)
                .verify();

        verify(branchRepository, never()).existsById(anyString(), anyString());
    }

    @Test
    void shouldRejectMissingStockWithoutQuerying() {
        StepVerifier.create(service.add(new AddProductCommand("f-1", "b-1", "Café", null)))
                .expectErrorMessage("El stock es obligatorio")
                .verify();

        verify(branchRepository, never()).existsById(anyString(), anyString());
    }

    @Test
    void shouldRejectBlankNameWithoutQuerying() {
        StepVerifier.create(service.add(new AddProductCommand("f-1", "b-1", " ", 3)))
                .expectError(DomainValidationException.class)
                .verify();

        verify(productRepository, never()).save(any());
    }
}
