package com.acenture.prueba_tecnica.product.infrastructure.in.web;

import com.acenture.prueba_tecnica.product.application.port.in.GetTopStockProductsUseCase;
import com.acenture.prueba_tecnica.product.infrastructure.in.web.dto.TopStockProductResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class TopStockProductHandler {

    private final GetTopStockProductsUseCase getTopStockProductsUseCase;

    public TopStockProductHandler(GetTopStockProductsUseCase getTopStockProductsUseCase) {
        this.getTopStockProductsUseCase = getTopStockProductsUseCase;
    }

    /**
     * Se junta la lista antes de responder para que un error (por ejemplo, 404)
     * se traduzca correctamente en vez de cortar una respuesta ya iniciada.
     */
    public Mono<ServerResponse> getByFranchise(ServerRequest request) {
        return getTopStockProductsUseCase.getByFranchise(request.pathVariable("franchiseId"))
                .map(TopStockProductResponse::from)
                .collectList()
                .flatMap(results -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(results));
    }
}
