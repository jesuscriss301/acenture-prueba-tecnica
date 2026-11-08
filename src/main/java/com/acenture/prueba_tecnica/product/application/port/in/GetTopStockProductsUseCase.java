package com.acenture.prueba_tecnica.product.application.port.in;

import com.acenture.prueba_tecnica.product.domain.model.BranchTopStockProduct;
import reactor.core.publisher.Flux;

/**
 * Puerto de entrada: producto con más stock en cada sucursal de una franquicia.
 */
public interface GetTopStockProductsUseCase {

    Flux<BranchTopStockProduct> getByFranchise(String franchiseId);
}
