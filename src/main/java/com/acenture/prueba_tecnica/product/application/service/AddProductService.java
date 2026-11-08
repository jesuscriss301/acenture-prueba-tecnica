package com.acenture.prueba_tecnica.product.application.service;

import com.acenture.prueba_tecnica.branch.application.port.out.BranchRepositoryPort;
import com.acenture.prueba_tecnica.product.application.port.in.AddProductUseCase;
import com.acenture.prueba_tecnica.product.application.port.out.ProductRepositoryPort;
import com.acenture.prueba_tecnica.product.domain.model.Product;
import com.acenture.prueba_tecnica.shared.domain.exception.ResourceNotFoundException;
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
