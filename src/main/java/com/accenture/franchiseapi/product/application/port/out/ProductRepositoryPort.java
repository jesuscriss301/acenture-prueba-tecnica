package com.accenture.franchiseapi.product.application.port.out;

import com.accenture.franchiseapi.product.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida: persistencia de productos.
 */
public interface ProductRepositoryPort {

    Mono<Product> save(Product product);

    Mono<Product> findById(String franchiseId, String branchId, String productId);

    /**
     * Reemplaza un producto existente.
     */
    Mono<Product> update(Product product);

    /**
     * Elimina el producto y lo devuelve. Vacío si no existía.
     */
    Mono<Product> deleteById(String franchiseId, String branchId, String productId);

    Flux<Product> findByFranchiseId(String franchiseId);
}
