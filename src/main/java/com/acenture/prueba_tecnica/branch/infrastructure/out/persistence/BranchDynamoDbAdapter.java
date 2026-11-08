package com.acenture.prueba_tecnica.branch.infrastructure.out.persistence;

import com.acenture.prueba_tecnica.branch.application.port.out.BranchRepositoryPort;
import com.acenture.prueba_tecnica.branch.domain.model.Branch;
import com.acenture.prueba_tecnica.shared.infrastructure.persistence.CatalogItem;
import com.acenture.prueba_tecnica.shared.infrastructure.persistence.DynamoDbKeys;
import com.acenture.prueba_tecnica.shared.infrastructure.persistence.DynamoDbQueries;
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
        return Mono.fromFuture(() -> table.getItem(
                        DynamoDbKeys.key(DynamoDbKeys.franchisePk(franchiseId), DynamoDbKeys.branchSk(branchId))))
                .map(item -> true)
                .defaultIfEmpty(false);
    }

    @Override
    public Flux<Branch> findByFranchiseId(String franchiseId) {
        return DynamoDbQueries.bySortKeyPrefix(table, DynamoDbKeys.franchisePk(franchiseId), DynamoDbKeys.BRANCH_PREFIX)
                .filter(item -> DynamoDbKeys.BRANCH_ENTITY.equals(item.getEntityType()))
                .map(BranchItemMapper::toDomain);
    }
}
