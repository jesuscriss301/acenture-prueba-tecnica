package com.acenture.prueba_tecnica.franchise.infrastructure.out.persistence;

import com.acenture.prueba_tecnica.franchise.application.port.out.FranchiseRepositoryPort;
import com.acenture.prueba_tecnica.franchise.domain.model.Franchise;
import com.acenture.prueba_tecnica.shared.infrastructure.persistence.CatalogItem;
import com.acenture.prueba_tecnica.shared.infrastructure.persistence.DynamoDbKeys;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;

/**
 * Adaptador de salida: implementa el puerto de persistencia con DynamoDB.
 * El cliente asíncrono devuelve CompletableFuture, que se convierte a Mono.
 */
@Component
public class FranchiseDynamoDbAdapter implements FranchiseRepositoryPort {

    private final DynamoDbAsyncTable<CatalogItem> table;

    public FranchiseDynamoDbAdapter(DynamoDbAsyncTable<CatalogItem> table) {
        this.table = table;
    }

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        PutItemEnhancedRequest<CatalogItem> request = PutItemEnhancedRequest.builder(CatalogItem.class)
                .item(FranchiseItemMapper.toItem(franchise))
                .conditionExpression(DynamoDbKeys.ITEM_MUST_NOT_EXIST)
                .build();

        return Mono.fromFuture(() -> table.putItem(request))
                .thenReturn(franchise);
    }

    @Override
    public Mono<Boolean> existsById(String franchiseId) {
        return findItem(franchiseId)
                .map(item -> true)
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Franchise> findById(String franchiseId) {
        return findItem(franchiseId).map(FranchiseItemMapper::toDomain);
    }

    @Override
    public Mono<Franchise> update(Franchise franchise) {
        PutItemEnhancedRequest<CatalogItem> request = PutItemEnhancedRequest.builder(CatalogItem.class)
                .item(FranchiseItemMapper.toItem(franchise))
                .conditionExpression(DynamoDbKeys.ITEM_MUST_EXIST)
                .build();

        return Mono.fromFuture(() -> table.putItem(request))
                .thenReturn(franchise);
    }

    private Mono<CatalogItem> findItem(String franchiseId) {
        return Mono.fromFuture(() -> table.getItem(
                DynamoDbKeys.key(DynamoDbKeys.franchisePk(franchiseId), DynamoDbKeys.METADATA)));
    }
}
