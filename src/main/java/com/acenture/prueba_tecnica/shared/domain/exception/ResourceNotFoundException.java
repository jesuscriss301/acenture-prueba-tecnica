package com.acenture.prueba_tecnica.shared.domain.exception;

/**
 * Se lanza cuando el recurso solicitado no existe. Se traduce a HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
