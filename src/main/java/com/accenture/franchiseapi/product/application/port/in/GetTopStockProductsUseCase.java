package com.accenture.franchiseapi.product.application.port.in;

import com.accenture.franchiseapi.product.domain.model.BranchTopStockProduct;
import reactor.core.publisher.Flux;

/**
 * Puerto de entrada: producto con más stock en cada sucursal de una franquicia.
 */
public interface GetTopStockProductsUseCase {

    Flux<BranchTopStockProduct> getByFranchise(String franchiseId);
}
