package com.acenture.prueba_tecnica.franchise.domain.model;

import com.acenture.prueba_tecnica.shared.domain.exception.DomainValidationException;

import java.util.UUID;

/**
 * Franquicia: entidad raíz del dominio. No depende de Spring ni de la base de datos.
 */
public record Franchise(String id, String name) {

    public static final int MAX_NAME_LENGTH = 100;

    public Franchise {
        if (id == null || id.isBlank()) {
            throw new DomainValidationException("El id de la franquicia es obligatorio");
        }
        name = validateName(name);
    }

    public static Franchise create(String name) {
        return new Franchise(UUID.randomUUID().toString(), name);
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("El nombre de la franquicia es obligatorio");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new DomainValidationException(
                    "El nombre de la franquicia no puede superar " + MAX_NAME_LENGTH + " caracteres");
        }
        return trimmed;
    }
}
