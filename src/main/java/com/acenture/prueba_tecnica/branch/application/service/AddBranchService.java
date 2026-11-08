package com.acenture.prueba_tecnica.branch.application.service;

import com.acenture.prueba_tecnica.branch.application.port.in.AddBranchUseCase;
import com.acenture.prueba_tecnica.branch.application.port.out.BranchRepositoryPort;
import com.acenture.prueba_tecnica.branch.domain.model.Branch;
import com.acenture.prueba_tecnica.franchise.application.port.out.FranchiseRepositoryPort;
import com.acenture.prueba_tecnica.shared.domain.exception.ResourceNotFoundException;
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
