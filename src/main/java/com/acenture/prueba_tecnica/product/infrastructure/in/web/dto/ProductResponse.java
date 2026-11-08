package com.acenture.prueba_tecnica.product.infrastructure.in.web.dto;

import com.acenture.prueba_tecnica.product.domain.model.Product;

public record ProductResponse(String id, String franchiseId, String branchId, String name, int stock) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.id(), product.franchiseId(), product.branchId(),
                product.name(), product.stock());
    }
}
