package com.acenture.prueba_tecnica.franchise.infrastructure.in.web;

import com.acenture.prueba_tecnica.franchise.application.port.in.CreateFranchiseUseCase;
import com.acenture.prueba_tecnica.franchise.application.port.in.CreateFranchiseUseCase.CreateFranchiseCommand;
import com.acenture.prueba_tecnica.franchise.application.port.in.RenameFranchiseUseCase;
import com.acenture.prueba_tecnica.franchise.application.port.in.RenameFranchiseUseCase.RenameFranchiseCommand;
import com.acenture.prueba_tecnica.franchise.infrastructure.in.web.dto.CreateFranchiseRequest;
import com.acenture.prueba_tecnica.franchise.infrastructure.in.web.dto.FranchiseResponse;
import com.acenture.prueba_tecnica.shared.domain.exception.DomainValidationException;
import com.acenture.prueba_tecnica.shared.infrastructure.web.dto.RenameRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class FranchiseHandler {

    private final CreateFranchiseUseCase createFranchiseUseCase;
    private final RenameFranchiseUseCase renameFranchiseUseCase;

    public FranchiseHandler(CreateFranchiseUseCase createFranchiseUseCase,
                            RenameFranchiseUseCase renameFranchiseUseCase) {
        this.createFranchiseUseCase = createFranchiseUseCase;
        this.renameFranchiseUseCase = renameFranchiseUseCase;
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

    public Mono<ServerResponse> rename(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        return request.bodyToMono(RenameRequest.class)
                .switchIfEmpty(Mono.error(new DomainValidationException("El cuerpo de la petición es obligatorio")))
                .map(body -> new RenameFranchiseCommand(franchiseId, body.name()))
                .flatMap(renameFranchiseUseCase::rename)
                .flatMap(franchise -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(FranchiseResponse.from(franchise)));
    }
}
