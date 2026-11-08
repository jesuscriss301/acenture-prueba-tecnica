package com.accenture.franchiseapi.franchise.application.service;

import com.accenture.franchiseapi.franchise.application.port.in.RenameFranchiseUseCase;
import com.accenture.franchiseapi.franchise.application.port.out.FranchiseRepositoryPort;
import com.accenture.franchiseapi.franchise.domain.model.Franchise;
import com.accenture.franchiseapi.shared.domain.exception.ResourceNotFoundException;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class RenameFranchiseService implements RenameFranchiseUseCase {

    private final FranchiseRepositoryPort franchiseRepository;

    public RenameFranchiseService(FranchiseRepositoryPort franchiseRepository) {
        this.franchiseRepository = Objects.requireNonNull(franchiseRepository);
    }

    @Override
    public Mono<Franchise> rename(RenameFranchiseCommand command) {
        return franchiseRepository.findById(command.franchiseId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "La franquicia " + command.franchiseId() + " no existe")))
                .map(franchise -> franchise.rename(command.name()))
                .flatMap(franchiseRepository::update);
    }
}
