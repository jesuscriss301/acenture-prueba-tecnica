package com.accenture.franchiseapi.product.infrastructure.in.web.dto;

import com.accenture.franchiseapi.product.domain.model.Product;

public record ProductResponse(String id, String franchiseId, String branchId, String name, int stock) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.id(), product.franchiseId(), product.branchId(),
                product.name(), product.stock());
    }
}
