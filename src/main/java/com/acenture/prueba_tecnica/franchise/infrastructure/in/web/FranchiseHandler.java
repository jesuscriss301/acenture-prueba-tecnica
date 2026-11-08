package com.acenture.prueba_tecnica.franchise.infrastructure.in.web;

import com.acenture.prueba_tecnica.franchise.application.port.in.CreateFranchiseUseCase;
import com.acenture.prueba_tecnica.franchise.application.port.in.CreateFranchiseUseCase.CreateFranchiseCommand;
import com.acenture.prueba_tecnica.franchise.infrastructure.in.web.dto.CreateFranchiseRequest;
import com.acenture.prueba_tecnica.franchise.infrastructure.in.web.dto.FranchiseResponse;
import com.acenture.prueba_tecnica.shared.domain.exception.DomainValidationException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class FranchiseHandler {

    private final CreateFranchiseUseCase createFranchiseUseCase;

    public FranchiseHandler(CreateFranchiseUseCase createFranchiseUseCase) {
        this.createFranchiseUseCase = createFranchiseUseCase;
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(CreateFranchiseRequest.class)
                .switchIfEmpty(Mono.error(new DomainValidationException("El cuerpo de la petición es obligatorio")))
                .map(body -> new CreateFranchiseCommand(body.name()))
                .flatMap(createFranchiseUseCase::create)
                .flatMap(franchise -> ServerResponse
                        .created(URI.create(FranchiseRouter.BASE_PATH + "/" + franchise.id()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(FranchiseResponse.from(franchise)));
    }
}
