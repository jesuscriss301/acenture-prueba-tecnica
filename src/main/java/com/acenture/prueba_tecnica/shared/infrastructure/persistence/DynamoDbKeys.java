package com.acenture.prueba_tecnica.shared.infrastructure.persistence;

/**
 * Diseño de tabla única (single-table design):
 *   Franquicia -> PK = FRANCHISE#{id}, SK = METADATA
 *   (más adelante) Sucursal -> PK = FRANCHISE#{id}, SK = BRANCH#{branchId}
 *   (más adelante) Producto -> PK = FRANCHISE#{id}, SK = BRANCH#{branchId}#PRODUCT#{productId}
 */
public final class DynamoDbKeys {

    public static final String FRANCHISE_PREFIX = "FRANCHISE#";
    public static final String METADATA = "METADATA";
    public static final String FRANCHISE_ENTITY = "FRANCHISE";

    private DynamoDbKeys() {
    }

    public static String franchisePk(String franchiseId) {
        return FRANCHISE_PREFIX + franchiseId;
    }
}
