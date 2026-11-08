package com.accenture.franchiseapi.branch.infrastructure.out.persistence;

import com.accenture.franchiseapi.branch.application.port.out.BranchRepositoryPort;
import com.accenture.franchiseapi.branch.domain.model.Branch;
import com.accenture.franchiseapi.shared.infrastructure.persistence.CatalogItem;
import com.accenture.franchiseapi.shared.infrastructure.persistence.DynamoDbKeys;
import com.accenture.franchiseapi.shared.infrastructure.persistence.DynamoDbQueries;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;

@Component
public class BranchDynamoDbAdapter implements BranchRepositoryPort {

    private final DynamoDbAsyncTable<CatalogItem> table;

    public BranchDynamoDbAdapter(DynamoDbAsyncTable<CatalogItem> table) {
        this.table = table;
    }

    @Override
    public Mono<Branch> save(Branch branch) {
        PutItemEnhancedRequest<CatalogItem> request = PutItemEnhancedRequest.builder(CatalogItem.class)
                .item(BranchItemMapper.toItem(branch))
                .conditionExpression(DynamoDbKeys.ITEM_MUST_NOT_EXIST)
                .build();

        return Mono.fromFuture(() -> table.putItem(request))
                .thenReturn(branch);
    }

    @Override
    public Mono<Boolean> existsById(String franchiseId, String branchId) {
        return findItem(franchiseId, branchId)
                .map(item -> true)
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Branch> findById(String franchiseId, String branchId) {
        return findItem(franchiseId, branchId).map(BranchItemMapper::toDomain);
    }

    @Override
    public Mono<Branch> update(Branch branch) {
        PutItemEnhancedRequest<CatalogItem> request = PutItemEnhancedRequest.builder(CatalogItem.class)
                .item(BranchItemMapper.toItem(branch))
                .conditionExpression(DynamoDbKeys.ITEM_MUST_EXIST)
                .build();

        return Mono.fromFuture(() -> table.putItem(request))
                .thenReturn(branch);
    }

    @Override
    public Flux<Branch> findByFranchiseId(String franchiseId) {
        return DynamoDbQueries.bySortKeyPrefix(table, DynamoDbKeys.franchisePk(franchiseId), DynamoDbKeys.BRANCH_PREFIX)
                .filter(item -> DynamoDbKeys.BRANCH_ENTITY.equals(item.getEntityType()))
                .map(BranchItemMapper::toDomain);
    }

    private Mono<CatalogItem> findItem(String franchiseId, String branchId) {
        return Mono.fromFuture(() -> table.getItem(
                DynamoDbKeys.key(DynamoDbKeys.franchisePk(franchiseId), DynamoDbKeys.branchSk(branchId))));
    }
}
