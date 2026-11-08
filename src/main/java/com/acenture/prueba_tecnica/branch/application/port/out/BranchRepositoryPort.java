package com.acenture.prueba_tecnica.branch.application.port.out;

import com.acenture.prueba_tecnica.branch.domain.model.Branch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida: persistencia de sucursales.
 */
public interface BranchRepositoryPort {

    Mono<Branch> save(Branch branch);

    Mono<Boolean> existsById(String franchiseId, String branchId);

    Flux<Branch> findByFranchiseId(String franchiseId);
}
