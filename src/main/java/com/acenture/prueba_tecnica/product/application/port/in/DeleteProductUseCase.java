package com.acenture.prueba_tecnica.product.application.port.in;

import reactor.core.publisher.Mono;

/**
 * Puerto de entrada: eliminar un producto de una sucursal.
 */
public interface DeleteProductUseCase {

    Mono<Void> delete(DeleteProductCommand command);

    record DeleteProductCommand(String franchiseId, String branchId, String productId) {
    }
}
