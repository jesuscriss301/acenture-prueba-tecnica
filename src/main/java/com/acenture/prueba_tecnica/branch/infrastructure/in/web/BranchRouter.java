package com.acenture.prueba_tecnica.branch.infrastructure.in.web;

import com.acenture.prueba_tecnica.shared.infrastructure.web.ApiErrorResponses;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class BranchRouter {

    public static final String BRANCHES_PATH = "/api/franchises/{franchiseId}/branches";

    @Bean
    public RouterFunction<ServerResponse> branchRoutes(BranchHandler handler) {
        return ApiErrorResponses.withErrorHandling(RouterFunctions.route()
                .POST(BRANCHES_PATH, handler::add));
    }
}
