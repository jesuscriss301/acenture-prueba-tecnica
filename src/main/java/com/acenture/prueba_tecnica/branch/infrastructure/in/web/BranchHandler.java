package com.acenture.prueba_tecnica.branch.infrastructure.in.web;

import com.acenture.prueba_tecnica.branch.application.port.in.AddBranchUseCase;
import com.acenture.prueba_tecnica.branch.application.port.in.AddBranchUseCase.AddBranchCommand;
import com.acenture.prueba_tecnica.branch.infrastructure.in.web.dto.AddBranchRequest;
import com.acenture.prueba_tecnica.branch.infrastructure.in.web.dto.BranchResponse;
import com.acenture.prueba_tecnica.shared.domain.exception.DomainValidationException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class BranchHandler {

    private final AddBranchUseCase addBranchUseCase;

    public BranchHandler(AddBranchUseCase addBranchUseCase) {
        this.addBranchUseCase = addBranchUseCase;
    }

    public Mono<ServerResponse> add(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        return request.bodyToMono(AddBranchRequest.class)
                .switchIfEmpty(Mono.error(new DomainValidationException("El cuerpo de la petición es obligatorio")))
                .map(body -> new AddBranchCommand(franchiseId, body.name()))
                .flatMap(addBranchUseCase::add)
                .flatMap(branch -> ServerResponse
                        .created(URI.create(request.path() + "/" + branch.id()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(BranchResponse.from(branch)));
    }
}
