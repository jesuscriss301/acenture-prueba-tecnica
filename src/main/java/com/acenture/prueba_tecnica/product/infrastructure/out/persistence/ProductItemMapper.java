package com.acenture.prueba_tecnica.product.infrastructure.out.persistence;

import com.acenture.prueba_tecnica.product.domain.model.Product;
import com.acenture.prueba_tecnica.shared.infrastructure.persistence.CatalogItem;
import com.acenture.prueba_tecnica.shared.infrastructure.persistence.DynamoDbKeys;

final class ProductItemMapper {

    private ProductItemMapper() {
    }

    static CatalogItem toItem(Product product) {
        CatalogItem item = new CatalogItem();
        item.setPk(DynamoDbKeys.franchisePk(product.franchiseId()));
        item.setSk(DynamoDbKeys.productSk(product.branchId(), product.id()));
        item.setEntityType(DynamoDbKeys.PRODUCT_ENTITY);
        item.setFranchiseId(product.franchiseId());
        item.setBranchId(product.branchId());
        item.setProductId(product.id());
        item.setName(product.name());
        item.setStock(product.stock());
        return item;
    }

    static Product toDomain(CatalogItem item) {
        return new Product(item.getProductId(), item.getFranchiseId(), item.getBranchId(),
                item.getName(), item.getStock());
    }
}
