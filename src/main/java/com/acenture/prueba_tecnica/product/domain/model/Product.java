package com.acenture.prueba_tecnica.product.domain.model;

import com.acenture.prueba_tecnica.shared.domain.validation.Validations;

import java.util.UUID;

/**
 * Producto: pertenece a una sucursal y tiene un stock que nunca es negativo.
 */
public record Product(String id, String franchiseId, String branchId, String name, int stock) {

    public Product {
        id = Validations.requireId(id, "El id del producto es obligatorio");
        franchiseId = Validations.requireId(franchiseId, "El id de la franquicia es obligatorio");
        branchId = Validations.requireId(branchId, "El id de la sucursal es obligatorio");
        name = Validations.requireName(name, "del producto");
        stock = Validations.requireStock(stock);
    }

    public static Product create(String franchiseId, String branchId, String name, Integer stock) {
        return new Product(UUID.randomUUID().toString(), franchiseId, branchId, name,
                Validations.requireStock(stock));
    }
}
