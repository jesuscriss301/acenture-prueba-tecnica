package com.acenture.prueba_tecnica.shared.domain.validation;

import com.acenture.prueba_tecnica.shared.domain.exception.DomainValidationException;

/**
 * Reglas de validación compartidas por las entidades del dominio.
 */
public final class Validations {

    public static final int MAX_NAME_LENGTH = 100;

    private Validations() {
    }

    public static String requireId(String id, String message) {
        if (id == null || id.isBlank()) {
            throw new DomainValidationException(message);
        }
        return id;
    }

    /**
     * @param owner complemento del mensaje, por ejemplo "de la franquicia" o "del producto"
     */
    public static String requireName(String name, String owner) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("El nombre " + owner + " es obligatorio");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new DomainValidationException(
                    "El nombre " + owner + " no puede superar " + MAX_NAME_LENGTH + " caracteres");
        }
        return trimmed;
    }

    public static int requireStock(Integer stock) {
        if (stock == null) {
            throw new DomainValidationException("El stock es obligatorio");
        }
        if (stock < 0) {
            throw new DomainValidationException("El stock no puede ser negativo");
        }
        return stock;
    }
}
