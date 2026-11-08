package com.acenture.prueba_tecnica.franchise.application.port.in;

import com.acenture.prueba_tecnica.franchise.domain.model.Franchise;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada: cambiar el nombre de una franquicia.
 */
public interface RenameFranchiseUseCase {

    Mono<Franchise> rename(RenameFranchiseCommand command);

    record RenameFranchiseCommand(String franchiseId, String name) {
    }
}
