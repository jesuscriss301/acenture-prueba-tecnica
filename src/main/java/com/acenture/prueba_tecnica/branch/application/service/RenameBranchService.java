package com.acenture.prueba_tecnica.branch.application.service;

import com.acenture.prueba_tecnica.branch.application.port.in.RenameBranchUseCase;
import com.acenture.prueba_tecnica.branch.application.port.out.BranchRepositoryPort;
import com.acenture.prueba_tecnica.branch.domain.model.Branch;
import com.acenture.prueba_tecnica.shared.domain.exception.ResourceNotFoundException;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class RenameBranchService implements RenameBranchUseCase {

    private final BranchRepositoryPort branchRepository;

    public RenameBranchService(BranchRepositoryPort branchRepository) {
        this.branchRepository = Objects.requireNonNull(branchRepository);
    }

    @Override
    public Mono<Branch> rename(RenameBranchCommand command) {
        return branchRepository.findById(command.franchiseId(), command.branchId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("La sucursal " + command.branchId()
                        + " no existe en la franquicia " + command.franchiseId())))
                .map(branch -> branch.rename(command.name()))
                .flatMap(branchRepository::update);
    }
}
