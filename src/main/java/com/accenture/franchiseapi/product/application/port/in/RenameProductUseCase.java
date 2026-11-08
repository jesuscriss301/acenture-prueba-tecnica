package com.accenture.franchiseapi.product.application.port.in;

import com.accenture.franchiseapi.product.domain.model.Product;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada: cambiar el nombre de un producto.
 */
public interface RenameProductUseCase {

    Mono<Product> rename(RenameProductCommand command);

    record RenameProductCommand(String franchiseId, String branchId, String productId, String name) {
    }
}
