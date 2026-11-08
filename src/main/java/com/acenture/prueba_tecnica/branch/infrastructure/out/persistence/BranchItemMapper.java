package com.acenture.prueba_tecnica.branch.infrastructure.out.persistence;

import com.acenture.prueba_tecnica.branch.domain.model.Branch;
import com.acenture.prueba_tecnica.shared.infrastructure.persistence.CatalogItem;
import com.acenture.prueba_tecnica.shared.infrastructure.persistence.DynamoDbKeys;

final class BranchItemMapper {

    private BranchItemMapper() {
    }

    static CatalogItem toItem(Branch branch) {
        CatalogItem item = new CatalogItem();
        item.setPk(DynamoDbKeys.franchisePk(branch.franchiseId()));
        item.setSk(DynamoDbKeys.branchSk(branch.id()));
        item.setEntityType(DynamoDbKeys.BRANCH_ENTITY);
        item.setFranchiseId(branch.franchiseId());
        item.setBranchId(branch.id());
        item.setName(branch.name());
        return item;
    }

    static Branch toDomain(CatalogItem item) {
        return new Branch(item.getBranchId(), item.getFranchiseId(), item.getName());
    }
}
