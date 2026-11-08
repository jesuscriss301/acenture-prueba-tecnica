package com.acenture.prueba_tecnica.franchise.infrastructure.out.persistence;

import com.acenture.prueba_tecnica.franchise.domain.model.Franchise;
import com.acenture.prueba_tecnica.shared.infrastructure.persistence.CatalogItem;
import com.acenture.prueba_tecnica.shared.infrastructure.persistence.DynamoDbKeys;

final class FranchiseItemMapper {

    private FranchiseItemMapper() {
    }

    static CatalogItem toItem(Franchise franchise) {
        CatalogItem item = new CatalogItem();
        item.setPk(DynamoDbKeys.franchisePk(franchise.id()));
        item.setSk(DynamoDbKeys.METADATA);
        item.setEntityType(DynamoDbKeys.FRANCHISE_ENTITY);
        item.setFranchiseId(franchise.id());
        item.setName(franchise.name());
        return item;
    }

    static Franchise toDomain(CatalogItem item) {
        return new Franchise(item.getFranchiseId(), item.getName());
    }
}
