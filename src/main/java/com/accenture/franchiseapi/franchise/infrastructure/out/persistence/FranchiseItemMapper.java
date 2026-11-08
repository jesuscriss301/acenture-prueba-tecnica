package com.accenture.franchiseapi.franchise.infrastructure.out.persistence;

import com.accenture.franchiseapi.franchise.domain.model.Franchise;
import com.accenture.franchiseapi.shared.infrastructure.persistence.CatalogItem;
import com.accenture.franchiseapi.shared.infrastructure.persistence.DynamoDbKeys;

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
