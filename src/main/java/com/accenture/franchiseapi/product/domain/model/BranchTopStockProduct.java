package com.accenture.franchiseapi.product.domain.model;

/**
 * Resultado de la consulta: el producto con más stock de una sucursal.
 */
public record BranchTopStockProduct(String branchId, String branchName,
                                    String productId, String productName, int stock) {
}
