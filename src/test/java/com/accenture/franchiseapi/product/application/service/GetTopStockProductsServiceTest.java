package com.accenture.franchiseapi.product.application.service;

import com.accenture.franchiseapi.branch.application.port.out.BranchRepositoryPort;
import com.accenture.franchiseapi.branch.domain.model.Branch;
import com.accenture.franchiseapi.franchise.application.port.out.FranchiseRepositoryPort;
import com.accenture.franchiseapi.product.application.port.out.ProductRepositoryPort;
import com.accenture.franchiseapi.product.domain.model.BranchTopStockProduct;
import com.accenture.franchiseapi.product.domain.model.Product;
import com.accenture.franchiseapi.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTopStockProductsServiceTest {

    private static final String FRANCHISE = "f-1";

    @Mock
    private FranchiseRepositoryPort franchiseRepository;
    @Mock
    private BranchRepositoryPort branchRepository;
    @Mock
    private ProductRepositoryPort productRepository;

    private GetTopStockProductsService service;

    @BeforeEach
    void setUp() {
        service = new GetTopStockProductsService(franchiseRepository, branchRepository, productRepository);
    }

    @Test
    void shouldReturnHighestStockProductPerBranchSortedByBranchName() {
        when(franchiseRepository.existsById(FRANCHISE)).thenReturn(Mono.just(true));
        when(branchRepository.findByFranchiseId(FRANCHISE)).thenReturn(Flux.just(
                new Branch("b-2", FRANCHISE, "Sucursal Norte"),
                new Branch("b-1", FRANCHISE, "Sucursal Centro"),
                new Branch("b-3", FRANCHISE, "Sucursal Vacía")));
        when(productRepository.findByFranchiseId(FRANCHISE)).thenReturn(Flux.just(
                new Product("p-1", FRANCHISE, "b-1", "Café", 10),
                new Product("p-2", FRANCHISE, "b-1", "Té", 30),
                new Product("p-3", FRANCHISE, "b-2", "Pan", 7)));

        StepVerifier.create(service.getByFranchise(FRANCHISE))
                .expectNext(new BranchTopStockProduct("b-1", "Sucursal Centro", "p-2", "Té", 30))
                .expectNext(new BranchTopStockProduct("b-2", "Sucursal Norte", "p-3", "Pan", 7))
                .verifyComplete();
    }

    @Test
    void shouldResolveTieByProductName() {
        when(franchiseRepository.existsById(FRANCHISE)).thenReturn(Mono.just(true));
        when(branchRepository.findByFranchiseId(FRANCHISE))
                .thenReturn(Flux.just(new Branch("b-1", FRANCHISE, "Sucursal Centro")));
        when(productRepository.findByFranchiseId(FRANCHISE)).thenReturn(Flux.just(
                new Product("p-1", FRANCHISE, "b-1", "Zumo", 15),
                new Product("p-2", FRANCHISE, "b-1", "Agua", 15)));

        StepVerifier.create(service.getByFranchise(FRANCHISE))
                .expectNext(new BranchTopStockProduct("b-1", "Sucursal Centro", "p-2", "Agua", 15))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenFranchiseHasNoProducts() {
        when(franchiseRepository.existsById(FRANCHISE)).thenReturn(Mono.just(true));
        when(branchRepository.findByFranchiseId(FRANCHISE)).thenReturn(Flux.empty());
        when(productRepository.findByFranchiseId(FRANCHISE)).thenReturn(Flux.empty());

        StepVerifier.create(service.getByFranchise(FRANCHISE))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenFranchiseDoesNotExist() {
        when(franchiseRepository.existsById("f-x")).thenReturn(Mono.just(false));

        StepVerifier.create(service.getByFranchise("f-x"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldIgnoreProductsOfUnknownBranches() {
        when(franchiseRepository.existsById(FRANCHISE)).thenReturn(Mono.just(true));
        when(branchRepository.findByFranchiseId(FRANCHISE))
                .thenReturn(Flux.just(new Branch("b-1", FRANCHISE, "Sucursal Centro")));
        when(productRepository.findByFranchiseId(FRANCHISE)).thenReturn(Flux.just(
                new Product("p-1", FRANCHISE, "b-1", "Café", 3),
                new Product("p-2", FRANCHISE, "b-borrada", "Té", 99)));

        StepVerifier.create(service.getByFranchise(FRANCHISE))
                .expectNext(new BranchTopStockProduct("b-1", "Sucursal Centro", "p-1", "Café", 3))
                .verifyComplete();
    }

    @Test
    void shouldIncludeProductWithZeroStock() {
        when(franchiseRepository.existsById(FRANCHISE)).thenReturn(Mono.just(true));
        when(branchRepository.findByFranchiseId(FRANCHISE))
                .thenReturn(Flux.just(new Branch("b-1", FRANCHISE, "Sucursal Centro")));
        when(productRepository.findByFranchiseId(FRANCHISE))
                .thenReturn(Flux.just(new Product("p-1", FRANCHISE, "b-1", "Café", 0)));

        StepVerifier.create(service.getByFranchise(FRANCHISE))
                .expectNext(new BranchTopStockProduct("b-1", "Sucursal Centro", "p-1", "Café", 0))
                .verifyComplete();
    }
}
