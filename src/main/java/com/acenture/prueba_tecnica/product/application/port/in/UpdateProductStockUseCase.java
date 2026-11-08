package com.acenture.prueba_tecnica.product.application.port.in;

import com.acenture.prueba_tecnica.product.domain.model.Product;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada: modificar el stock de un producto.
 */
public interface UpdateProductStockUseCase {

    Mono<Product> updateStock(UpdateProductStockCommand command);

    record UpdateProductStockCommand(String franchiseId, String branchId, String productId, Integer stock) {
    }
}
