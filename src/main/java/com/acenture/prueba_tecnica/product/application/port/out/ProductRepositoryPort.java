package com.acenture.prueba_tecnica.product.application.port.out;

import com.acenture.prueba_tecnica.product.domain.model.Product;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida: persistencia de productos.
 */
public interface ProductRepositoryPort {

    Mono<Product> save(Product product);
}
