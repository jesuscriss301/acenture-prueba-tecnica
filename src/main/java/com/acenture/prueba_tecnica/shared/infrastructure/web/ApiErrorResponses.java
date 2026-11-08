package com.acenture.prueba_tecnica.shared.infrastructure.web;

import com.acenture.prueba_tecnica.shared.domain.exception.DomainValidationException;
import com.acenture.prueba_tecnica.shared.domain.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

/**
 * Manejo de errores con un formato JSON único para toda la API.
 */
public final class ApiErrorResponses {

    private ApiErrorResponses() {
    }

    /**
     * Agrega a un grupo de rutas la traducción de excepciones a respuestas HTTP.
     */
    public static RouterFunction<ServerResponse> withErrorHandling(RouterFunctions.Builder routes) {
        return routes
                .onError(DomainValidationException.class,
                        (error, request) -> badRequest(error.getMessage()))
                .onError(ResourceNotFoundException.class,
                        (error, request) -> notFound(error.getMessage()))
                .onError(ServerWebInputException.class,
                        (error, request) -> badRequest("El cuerpo de la petición no es un JSON válido"))
                .build();
    }

    public static Mono<ServerResponse> badRequest(String message) {
        return error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message);
    }

    public static Mono<ServerResponse> notFound(String message) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", message);
    }

    private static Mono<ServerResponse> error(HttpStatus status, String code, String message) {
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ErrorResponse(code, message));
    }
}
