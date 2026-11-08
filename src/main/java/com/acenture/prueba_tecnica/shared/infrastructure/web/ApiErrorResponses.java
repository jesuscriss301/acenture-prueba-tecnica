package com.acenture.prueba_tecnica.shared.infrastructure.web;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Respuestas de error con un formato JSON único para toda la API.
 */
public final class ApiErrorResponses {

    private ApiErrorResponses() {
    }

    public static Mono<ServerResponse> badRequest(String message) {
        return ServerResponse.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ErrorResponse("BAD_REQUEST", message));
    }
}
