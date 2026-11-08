package com.acenture.prueba_tecnica.product.application.service;

import com.acenture.prueba_tecnica.product.application.port.in.RenameProductUseCase;
import com.acenture.prueba_tecnica.product.application.port.out.ProductRepositoryPort;
import com.acenture.prueba_tecnica.product.domain.model.Product;
import com.acenture.prueba_tecnica.shared.domain.exception.ResourceNotFoundException;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class RenameProductService implements RenameProductUseCase {

    private final ProductRepositoryPort productRepository;

    public RenameProductService(ProductRepositoryPort productRepository) {
        this.productRepository = Objects.requireNonNull(productRepository);
    }

    @Override
    public Mono<Product> rename(RenameProductCommand command) {
        return productRepository.findById(command.franchiseId(), command.branchId(), command.productId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("El producto " + command.productId()
                        + " no existe en la sucursal " + command.branchId())))
                .map(product -> product.rename(command.name()))
                .flatMap(productRepository::update);
    }
}
