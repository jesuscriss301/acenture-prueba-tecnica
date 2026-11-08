package com.acenture.prueba_tecnica.product.infrastructure.in.web;

import com.acenture.prueba_tecnica.shared.infrastructure.web.ApiErrorResponses;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ProductRouter {

    public static final String PRODUCTS_PATH = "/api/franchises/{franchiseId}/branches/{branchId}/products";

    @Bean
    public RouterFunction<ServerResponse> productRoutes(ProductHandler handler) {
        return ApiErrorResponses.withErrorHandling(RouterFunctions.route()
                .POST(PRODUCTS_PATH, handler::add));
    }
}
