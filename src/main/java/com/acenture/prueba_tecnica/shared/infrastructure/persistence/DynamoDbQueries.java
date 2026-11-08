package com.acenture.prueba_tecnica.shared.infrastructure.persistence;

import reactor.core.publisher.Flux;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

/**
 * Consultas reutilizables sobre la tabla única.
 */
public final class DynamoDbQueries {

    private DynamoDbQueries() {
    }

    /**
     * Lee todos los ítems de una partición cuyo SK empieza por el prefijo dado.
     * La paginación de DynamoDB queda oculta: se recibe un Flux continuo.
     */
    public static Flux<CatalogItem> bySortKeyPrefix(DynamoDbAsyncTable<CatalogItem> table,
                                                    String pk, String skPrefix) {
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.sortBeginsWith(DynamoDbKeys.key(pk, skPrefix)))
                .build();
        return Flux.from(table.query(request).items());
    }
}
