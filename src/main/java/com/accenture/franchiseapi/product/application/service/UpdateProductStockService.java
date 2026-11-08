package com.accenture.franchiseapi.product.application.service;

import com.accenture.franchiseapi.product.application.port.in.UpdateProductStockUseCase;
import com.accenture.franchiseapi.product.application.port.out.ProductRepositoryPort;
import com.accenture.franchiseapi.product.domain.model.Product;
import com.accenture.franchiseapi.shared.domain.exception.ResourceNotFoundException;
import com.accenture.franchiseapi.shared.domain.validation.Validations;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Caso de uso: modificar stock. Valida el valor antes de consultar la base.
 */
public class UpdateProductStockService implements UpdateProductStockUseCase {

    private final ProductRepositoryPort productRepository;

    public UpdateProductStockService(ProductRepositoryPort productRepository) {
        this.productRepository = Objects.requireNonNull(productRepository);
    }

    @Override
    public Mono<Product> updateStock(UpdateProductStockCommand command) {
        return Mono.fromCallable(() -> Validations.requireStock(command.stock()))
                .flatMap(stock -> productRepository
                        .findById(command.franchiseId(), command.branchId(), command.productId())
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("El producto " + command.productId()
                                + " no existe en la sucursal " + command.branchId())))
                        .map(product -> product.withStock(stock)))
                .flatMap(productRepository::update);
    }
}
