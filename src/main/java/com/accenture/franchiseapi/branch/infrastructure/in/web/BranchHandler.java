package com.accenture.franchiseapi.branch.infrastructure.in.web;

import com.accenture.franchiseapi.branch.application.port.in.AddBranchUseCase;
import com.accenture.franchiseapi.branch.application.port.in.AddBranchUseCase.AddBranchCommand;
import com.accenture.franchiseapi.branch.application.port.in.RenameBranchUseCase;
import com.accenture.franchiseapi.branch.application.port.in.RenameBranchUseCase.RenameBranchCommand;
import com.accenture.franchiseapi.branch.infrastructure.in.web.dto.AddBranchRequest;
import com.accenture.franchiseapi.branch.infrastructure.in.web.dto.BranchResponse;
import com.accenture.franchiseapi.shared.domain.exception.DomainValidationException;
import com.accenture.franchiseapi.shared.infrastructure.web.dto.RenameRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class BranchHandler {

    private final AddBranchUseCase addBranchUseCase;
    private final RenameBranchUseCase renameBranchUseCase;

    public BranchHandler(AddBranchUseCase addBranchUseCase, RenameBranchUseCase renameBranchUseCase) {
        this.addBranchUseCase = addBranchUseCase;
        this.renameBranchUseCase = renameBranchUseCase;
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

    public Mono<ServerResponse> rename(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        return request.bodyToMono(RenameRequest.class)
                .switchIfEmpty(Mono.error(new DomainValidationException("El cuerpo de la petición es obligatorio")))
                .map(body -> new RenameBranchCommand(franchiseId, branchId, body.name()))
                .flatMap(renameBranchUseCase::rename)
                .flatMap(branch -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(BranchResponse.from(branch)));
    }
}
