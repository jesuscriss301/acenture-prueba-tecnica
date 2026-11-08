package com.acenture.prueba_tecnica.franchise.application.port.in;

import com.acenture.prueba_tecnica.franchise.domain.model.Franchise;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada: crear una nueva franquicia.
 */
public interface CreateFranchiseUseCase {

    Mono<Franchise> create(CreateFranchiseCommand command);

    record CreateFranchiseCommand(String name) {
    }
}
