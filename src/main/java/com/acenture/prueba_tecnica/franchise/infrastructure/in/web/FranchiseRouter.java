package com.acenture.prueba_tecnica.franchise.infrastructure.in.web;

import com.acenture.prueba_tecnica.shared.domain.exception.DomainValidationException;
import com.acenture.prueba_tecnica.shared.infrastructure.web.ApiErrorResponses;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;

@Configuration
public class FranchiseRouter {

    public static final String BASE_PATH = "/api/franchises";

    @Bean
    public RouterFunction<ServerResponse> franchiseRoutes(FranchiseHandler handler) {
        return RouterFunctions.route()
                .POST(BASE_PATH, handler::create)
                .onError(DomainValidationException.class,
                        (error, request) -> ApiErrorResponses.badRequest(error.getMessage()))
                .onError(ServerWebInputException.class,
                        (error, request) -> ApiErrorResponses.badRequest("El cuerpo de la petición no es un JSON válido"))
                .build();
    }
}
