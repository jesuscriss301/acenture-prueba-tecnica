package com.acenture.prueba_tecnica.franchise.infrastructure.in.web;

import com.acenture.prueba_tecnica.shared.infrastructure.web.ApiErrorResponses;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class FranchiseRouter {

    public static final String BASE_PATH = "/api/franchises";

    @Bean
    public RouterFunction<ServerResponse> franchiseRoutes(FranchiseHandler handler) {
        return ApiErrorResponses.withErrorHandling(RouterFunctions.route()
                .POST(BASE_PATH, handler::create)
                .PATCH(BASE_PATH + "/{franchiseId}/name", handler::rename));
    }
}
