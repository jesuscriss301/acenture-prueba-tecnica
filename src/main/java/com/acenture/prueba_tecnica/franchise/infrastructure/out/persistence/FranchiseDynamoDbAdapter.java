package com.acenture.prueba_tecnica.franchise.infrastructure.out.persistence;

import com.acenture.prueba_tecnica.franchise.application.port.out.FranchiseRepositoryPort;
import com.acenture.prueba_tecnica.franchise.domain.model.Franchise;
import com.acenture.prueba_tecnica.shared.infrastructure.config.DynamoDbProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;

/**
 * Adaptador de salida: implementa el puerto de persistencia con DynamoDB.
 * El cliente asíncrono devuelve CompletableFuture, que se convierte a Mono.
 */
@Component
public class FranchiseDynamoDbAdapter implements FranchiseRepositoryPort {

    private static final Expression ITEM_MUST_NOT_EXIST = Expression.builder()
            .expression("attribute_not_exists(PK)")
            .build();

    private final DynamoDbAsyncTable<FranchiseItem> table;

    public FranchiseDynamoDbAdapter(DynamoDbEnhancedAsyncClient enhancedClient, DynamoDbProperties properties) {
        this.table = enhancedClient.table(properties.tableName(), TableSchema.fromBean(FranchiseItem.class));
    }

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        PutItemEnhancedRequest<FranchiseItem> request = PutItemEnhancedRequest.builder(FranchiseItem.class)
                .item(FranchiseItemMapper.toItem(franchise))
                .conditionExpression(ITEM_MUST_NOT_EXIST)
                .build();

        return Mono.fromFuture(() -> table.putItem(request))
                .thenReturn(franchise);
    }
}
