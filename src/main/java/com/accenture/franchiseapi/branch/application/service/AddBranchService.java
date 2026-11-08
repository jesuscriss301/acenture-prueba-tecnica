package com.accenture.franchiseapi.branch.application.service;

import com.accenture.franchiseapi.branch.application.port.in.AddBranchUseCase;
import com.accenture.franchiseapi.branch.application.port.out.BranchRepositoryPort;
import com.accenture.franchiseapi.branch.domain.model.Branch;
import com.accenture.franchiseapi.franchise.application.port.out.FranchiseRepositoryPort;
import com.accenture.franchiseapi.shared.domain.exception.ResourceNotFoundException;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Caso de uso: agregar sucursal. Primero valida los datos (sin ir a la base)
 * y luego confirma que la franquicia exista.
 */
public class AddBranchService implements AddBranchUseCase {

    private final FranchiseRepositoryPort franchiseRepository;
    private final BranchRepositoryPort branchRepository;

    public AddBranchService(FranchiseRepositoryPort franchiseRepository, BranchRepositoryPort branchRepository) {
        this.franchiseRepository = Objects.requireNonNull(franchiseRepository);
        this.branchRepository = Objects.requireNonNull(branchRepository);
    }

    @Override
    public Mono<Branch> add(AddBranchCommand command) {
        return Mono.fromCallable(() -> Branch.create(command.franchiseId(), command.name()))
                .flatMap(branch -> franchiseRepository.existsById(branch.franchiseId())
                        .flatMap(exists -> exists
                                ? branchRepository.save(branch)
                                : Mono.error(new ResourceNotFoundException(
                                        "La franquicia " + branch.franchiseId() + " no existe"))));
    }
}
