package com.acenture.prueba_tecnica.product.infrastructure.in.web;

import com.acenture.prueba_tecnica.product.application.port.in.AddProductUseCase;
import com.acenture.prueba_tecnica.product.application.port.in.AddProductUseCase.AddProductCommand;
import com.acenture.prueba_tecnica.product.application.port.in.DeleteProductUseCase;
import com.acenture.prueba_tecnica.product.application.port.in.DeleteProductUseCase.DeleteProductCommand;
import com.acenture.prueba_tecnica.product.infrastructure.in.web.dto.AddProductRequest;
import com.acenture.prueba_tecnica.product.infrastructure.in.web.dto.ProductResponse;
import com.acenture.prueba_tecnica.shared.domain.exception.DomainValidationException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class ProductHandler {

    private final AddProductUseCase addProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;

    public ProductHandler(AddProductUseCase addProductUseCase, DeleteProductUseCase deleteProductUseCase) {
        this.addProductUseCase = addProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
    }

    public Mono<ServerResponse> add(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        return request.bodyToMono(AddProductRequest.class)
                .switchIfEmpty(Mono.error(new DomainValidationException("El cuerpo de la petición es obligatorio")))
                .map(body -> new AddProductCommand(franchiseId, branchId, body.name(), body.stock()))
                .flatMap(addProductUseCase::add)
                .flatMap(product -> ServerResponse
                        .created(URI.create(request.path() + "/" + product.id()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ProductResponse.from(product)));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        DeleteProductCommand command = new DeleteProductCommand(
                request.pathVariable("franchiseId"),
                request.pathVariable("branchId"),
                request.pathVariable("productId"));
        return deleteProductUseCase.delete(command)
                .then(ServerResponse.noContent().build());
    }
}
