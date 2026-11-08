package com.accenture.franchiseapi.branch.infrastructure.out.persistence;

import com.accenture.franchiseapi.branch.domain.model.Branch;
import com.accenture.franchiseapi.shared.infrastructure.persistence.CatalogItem;
import com.accenture.franchiseapi.shared.infrastructure.persistence.DynamoDbKeys;

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
