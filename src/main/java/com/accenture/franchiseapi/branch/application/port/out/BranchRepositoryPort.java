package com.accenture.franchiseapi.branch.application.port.out;

import com.accenture.franchiseapi.branch.domain.model.Branch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida: persistencia de sucursales.
 */
public interface BranchRepositoryPort {

    Mono<Branch> save(Branch branch);

    Mono<Boolean> existsById(String franchiseId, String branchId);

    Flux<Branch> findByFranchiseId(String franchiseId);

    Mono<Branch> findById(String franchiseId, String branchId);

    /**
     * Reemplaza una sucursal existente.
     */
    Mono<Branch> update(Branch branch);
}
