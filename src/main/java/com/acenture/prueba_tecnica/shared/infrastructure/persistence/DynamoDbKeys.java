package com.acenture.prueba_tecnica.shared.infrastructure.persistence;

import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;

/**
 * Diseño de tabla única (single-table design). Todo lo de una franquicia
 * comparte la misma partición, así se puede leer con una sola consulta:
 *   Franquicia -> PK = FRANCHISE#{franchiseId}, SK = METADATA
 *   Sucursal   -> PK = FRANCHISE#{franchiseId}, SK = BRANCH#{branchId}
 *   Producto   -> PK = FRANCHISE#{franchiseId}, SK = BRANCH#{branchId}#PRODUCT#{productId}
 */
public final class DynamoDbKeys {

    public static final String FRANCHISE_PREFIX = "FRANCHISE#";
    public static final String BRANCH_PREFIX = "BRANCH#";
    public static final String PRODUCT_SEPARATOR = "#PRODUCT#";
    public static final String METADATA = "METADATA";

    public static final String FRANCHISE_ENTITY = "FRANCHISE";
    public static final String BRANCH_ENTITY = "BRANCH";
    public static final String PRODUCT_ENTITY = "PRODUCT";

    public static final Expression ITEM_MUST_NOT_EXIST = Expression.builder()
            .expression("attribute_not_exists(PK)")
            .build();

    public static final Expression ITEM_MUST_EXIST = Expression.builder()
            .expression("attribute_exists(PK)")
            .build();

    private DynamoDbKeys() {
    }

    public static String franchisePk(String franchiseId) {
        return FRANCHISE_PREFIX + franchiseId;
    }

    public static String branchSk(String branchId) {
        return BRANCH_PREFIX + branchId;
    }

    public static String productSk(String branchId, String productId) {
        return branchSk(branchId) + PRODUCT_SEPARATOR + productId;
    }

    public static Key key(String pk, String sk) {
        return Key.builder().partitionValue(pk).sortValue(sk).build();
    }
}
