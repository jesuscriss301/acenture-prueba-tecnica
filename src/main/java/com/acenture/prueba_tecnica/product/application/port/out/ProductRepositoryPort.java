package com.acenture.prueba_tecnica.product.application.port.out;

import com.acenture.prueba_tecnica.product.domain.model.Product;
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
}
