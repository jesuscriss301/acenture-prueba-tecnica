package com.acenture.prueba_tecnica.product.application.port.in;

import com.acenture.prueba_tecnica.product.domain.model.Product;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada: agregar un producto a una sucursal.
 */
public interface AddProductUseCase {

    Mono<Product> add(AddProductCommand command);

    record AddProductCommand(String franchiseId, String branchId, String name, Integer stock) {
    }
}
