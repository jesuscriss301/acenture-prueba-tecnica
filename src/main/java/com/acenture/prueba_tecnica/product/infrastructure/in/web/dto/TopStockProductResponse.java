package com.acenture.prueba_tecnica.product.infrastructure.in.web.dto;

import com.acenture.prueba_tecnica.product.domain.model.BranchTopStockProduct;

public record TopStockProductResponse(String branchId, String branchName,
                                      String productId, String productName, int stock) {

    public static TopStockProductResponse from(BranchTopStockProduct result) {
        return new TopStockProductResponse(result.branchId(), result.branchName(),
                result.productId(), result.productName(), result.stock());
    }
}
