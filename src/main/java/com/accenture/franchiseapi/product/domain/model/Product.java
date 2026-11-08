package com.accenture.franchiseapi.product.domain.model;

import com.accenture.franchiseapi.shared.domain.validation.Validations;

import java.util.Comparator;
import java.util.UUID;

/**
 * Producto: pertenece a una sucursal y tiene un stock que nunca es negativo.
 */
public record Product(String id, String franchiseId, String branchId, String name, int stock) {

    /**
     * Orden de "mayor stock": primero el de más stock; si empatan, por nombre y luego por id
     * para que el resultado sea siempre el mismo.
     */
    public static final Comparator<Product> HIGHEST_STOCK_FIRST = Comparator
            .comparingInt(Product::stock).reversed()
            .thenComparing(Product::name)
            .thenComparing(Product::id);

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

    public static Product higherStock(Product first, Product second) {
        return HIGHEST_STOCK_FIRST.compare(first, second) <= 0 ? first : second;
    }

    public Product withStock(Integer newStock) {
        return new Product(id, franchiseId, branchId, name, Validations.requireStock(newStock));
    }

    public Product rename(String newName) {
        return new Product(id, franchiseId, branchId, newName, stock);
    }
}
