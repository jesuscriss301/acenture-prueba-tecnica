package com.acenture.prueba_tecnica.product.infrastructure.out.persistence;

import com.acenture.prueba_tecnica.product.application.port.out.ProductRepositoryPort;
import com.acenture.prueba_tecnica.product.domain.model.Product;
import com.acenture.prueba_tecnica.shared.infrastructure.persistence.CatalogItem;
import com.acenture.prueba_tecnica.shared.infrastructure.persistence.DynamoDbKeys;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;

@Component
public class ProductDynamoDbAdapter implements ProductRepositoryPort {

    private final DynamoDbAsyncTable<CatalogItem> table;

    public ProductDynamoDbAdapter(DynamoDbAsyncTable<CatalogItem> table) {
        this.table = table;
    }

    @Override
    public Mono<Product> save(Product product) {
        PutItemEnhancedRequest<CatalogItem> request = PutItemEnhancedRequest.builder(CatalogItem.class)
                .item(ProductItemMapper.toItem(product))
                .conditionExpression(DynamoDbKeys.ITEM_MUST_NOT_EXIST)
                .build();

        return Mono.fromFuture(() -> table.putItem(request))
                .thenReturn(product);
    }

    @Override
    public Mono<Product> findById(String franchiseId, String branchId, String productId) {
        return Mono.fromFuture(() -> table.getItem(productKey(franchiseId, branchId, productId)))
                .map(ProductItemMapper::toDomain);
    }

    @Override
    public Mono<Product> update(Product product) {
        PutItemEnhancedRequest<CatalogItem> request = PutItemEnhancedRequest.builder(CatalogItem.class)
                .item(ProductItemMapper.toItem(product))
                .conditionExpression(DynamoDbKeys.ITEM_MUST_EXIST)
                .build();

        return Mono.fromFuture(() -> table.putItem(request))
                .thenReturn(product);
    }

    @Override
    public Mono<Product> deleteById(String franchiseId, String branchId, String productId) {
        return Mono.fromFuture(() -> table.deleteItem(productKey(franchiseId, branchId, productId)))
                .map(ProductItemMapper::toDomain);
    }

    private static Key productKey(String franchiseId, String branchId, String productId) {
        return DynamoDbKeys.key(DynamoDbKeys.franchisePk(franchiseId), DynamoDbKeys.productSk(branchId, productId));
    }
}
