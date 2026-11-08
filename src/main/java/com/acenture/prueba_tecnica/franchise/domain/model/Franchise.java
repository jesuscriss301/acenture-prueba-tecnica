package com.acenture.prueba_tecnica.franchise.domain.model;

import com.acenture.prueba_tecnica.shared.domain.validation.Validations;

import java.util.UUID;

/**
 * Franquicia: entidad raíz del dominio. No depende de Spring ni de la base de datos.
 */
public record Franchise(String id, String name) {

    public static final int MAX_NAME_LENGTH = Validations.MAX_NAME_LENGTH;

    public Franchise {
        id = Validations.requireId(id, "El id de la franquicia es obligatorio");
        name = Validations.requireName(name, "de la franquicia");
    }

    public static Franchise create(String name) {
        return new Franchise(UUID.randomUUID().toString(), name);
    }
}
