package com.accenture.franchiseapi.franchise.application.service;

import com.accenture.franchiseapi.franchise.application.port.in.CreateFranchiseUseCase;
import com.accenture.franchiseapi.franchise.application.port.out.FranchiseRepositoryPort;
import com.accenture.franchiseapi.franchise.domain.model.Franchise;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Caso de uso: crear franquicia. Sin anotaciones de Spring;
 * se registra como bean en FranchiseBeanConfig.
 */
public class CreateFranchiseService implements CreateFranchiseUseCase {

    private final FranchiseRepositoryPort franchiseRepository;

    public CreateFranchiseService(FranchiseRepositoryPort franchiseRepository) {
        this.franchiseRepository = Objects.requireNonNull(franchiseRepository);
    }

    @Override
    public Mono<Franchise> create(CreateFranchiseCommand command) {
        return Mono.fromCallable(() -> Franchise.create(command.name()))
                .flatMap(franchiseRepository::save);
    }
}
