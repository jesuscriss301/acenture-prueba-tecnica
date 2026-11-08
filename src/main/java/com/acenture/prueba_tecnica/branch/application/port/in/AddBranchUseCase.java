package com.acenture.prueba_tecnica.branch.application.port.in;

import com.acenture.prueba_tecnica.branch.domain.model.Branch;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada: agregar una sucursal a una franquicia.
 */
public interface AddBranchUseCase {

    Mono<Branch> add(AddBranchCommand command);

    record AddBranchCommand(String franchiseId, String name) {
    }
}
