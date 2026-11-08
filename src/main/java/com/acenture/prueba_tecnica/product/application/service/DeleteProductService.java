package com.acenture.prueba_tecnica.product.application.service;

import com.acenture.prueba_tecnica.product.application.port.in.DeleteProductUseCase;
import com.acenture.prueba_tecnica.product.application.port.out.ProductRepositoryPort;
import com.acenture.prueba_tecnica.shared.domain.exception.ResourceNotFoundException;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Caso de uso: eliminar producto. Si no existía, responde 404.
 */
public class DeleteProductService implements DeleteProductUseCase {

    private final ProductRepositoryPort productRepository;

    public DeleteProductService(ProductRepositoryPort productRepository) {
        this.productRepository = Objects.requireNonNull(productRepository);
    }

    @Override
    public Mono<Void> delete(DeleteProductCommand command) {
        return productRepository.deleteById(command.franchiseId(), command.branchId(), command.productId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("El producto " + command.productId()
                        + " no existe en la sucursal " + command.branchId())))
                .then();
    }
}
