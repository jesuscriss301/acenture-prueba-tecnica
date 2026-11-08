package com.acenture.prueba_tecnica.product.application.service;

import com.acenture.prueba_tecnica.product.application.port.in.UpdateProductStockUseCase;
import com.acenture.prueba_tecnica.product.application.port.out.ProductRepositoryPort;
import com.acenture.prueba_tecnica.product.domain.model.Product;
import com.acenture.prueba_tecnica.shared.domain.exception.ResourceNotFoundException;
import com.acenture.prueba_tecnica.shared.domain.validation.Validations;
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
