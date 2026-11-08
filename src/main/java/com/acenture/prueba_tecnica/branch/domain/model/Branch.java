package com.acenture.prueba_tecnica.branch.domain.model;

import com.acenture.prueba_tecnica.shared.domain.validation.Validations;

import java.util.UUID;

/**
 * Sucursal: pertenece a una franquicia.
 */
public record Branch(String id, String franchiseId, String name) {

    public Branch {
        id = Validations.requireId(id, "El id de la sucursal es obligatorio");
        franchiseId = Validations.requireId(franchiseId, "El id de la franquicia es obligatorio");
        name = Validations.requireName(name, "de la sucursal");
    }

    public static Branch create(String franchiseId, String name) {
        return new Branch(UUID.randomUUID().toString(), franchiseId, name);
    }

    public Branch rename(String newName) {
        return new Branch(id, franchiseId, newName);
    }
}
