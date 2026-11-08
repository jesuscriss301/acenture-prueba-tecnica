package com.accenture.franchiseapi.shared.domain.exception;

/**
 * Se lanza cuando un dato de entrada rompe una regla del dominio
 * (por ejemplo, un nombre vacío). Se traduce a HTTP 400.
 */
public class DomainValidationException extends RuntimeException {

    public DomainValidationException(String message) {
        super(message);
    }
}
