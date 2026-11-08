package com.accenture.franchiseapi.product.application.port.in;

import com.accenture.franchiseapi.product.domain.model.Product;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada: modificar el stock de un producto.
 */
public interface UpdateProductStockUseCase {

    Mono<Product> updateStock(UpdateProductStockCommand command);

    record UpdateProductStockCommand(String franchiseId, String branchId, String productId, Integer stock) {
    }
}
