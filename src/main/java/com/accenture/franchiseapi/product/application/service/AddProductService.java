package com.accenture.franchiseapi.product.application.service;

import com.accenture.franchiseapi.branch.application.port.out.BranchRepositoryPort;
import com.accenture.franchiseapi.product.application.port.in.AddProductUseCase;
import com.accenture.franchiseapi.product.application.port.out.ProductRepositoryPort;
import com.accenture.franchiseapi.product.domain.model.Product;
import com.accenture.franchiseapi.shared.domain.exception.ResourceNotFoundException;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Caso de uso: agregar producto a una sucursal existente.
 */
public class AddProductService implements AddProductUseCase {

    private final BranchRepositoryPort branchRepository;
    private final ProductRepositoryPort productRepository;

    public AddProductService(BranchRepositoryPort branchRepository, ProductRepositoryPort productRepository) {
        this.branchRepository = Objects.requireNonNull(branchRepository);
        this.productRepository = Objects.requireNonNull(productRepository);
    }

    @Override
    public Mono<Product> add(AddProductCommand command) {
        return Mono.fromCallable(() -> Product.create(
                        command.franchiseId(), command.branchId(), command.name(), command.stock()))
                .flatMap(product -> branchRepository.existsById(product.franchiseId(), product.branchId())
                        .flatMap(exists -> exists
                                ? productRepository.save(product)
                                : Mono.error(new ResourceNotFoundException("La sucursal " + product.branchId()
                                        + " no existe en la franquicia " + product.franchiseId()))));
    }
}
