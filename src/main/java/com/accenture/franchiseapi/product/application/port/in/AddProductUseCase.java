package com.accenture.franchiseapi.product.application.port.in;

import com.accenture.franchiseapi.product.domain.model.Product;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada: agregar un producto a una sucursal.
 */
public interface AddProductUseCase {

    Mono<Product> add(AddProductCommand command);

    record AddProductCommand(String franchiseId, String branchId, String name, Integer stock) {
    }
}
