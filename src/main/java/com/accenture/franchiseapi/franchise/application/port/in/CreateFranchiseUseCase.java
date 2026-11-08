package com.accenture.franchiseapi.franchise.application.port.in;

import com.accenture.franchiseapi.franchise.domain.model.Franchise;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada: crear una nueva franquicia.
 */
public interface CreateFranchiseUseCase {

    Mono<Franchise> create(CreateFranchiseCommand command);

    record CreateFranchiseCommand(String name) {
    }
}
