package com.accenture.franchiseapi.franchise.application.port.out;

import com.accenture.franchiseapi.franchise.domain.model.Franchise;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida: persistencia de franquicias.
 * La implementación concreta (DynamoDB) vive en infraestructura.
 */
public interface FranchiseRepositoryPort {

    Mono<Franchise> save(Franchise franchise);

    Mono<Boolean> existsById(String franchiseId);

    Mono<Franchise> findById(String franchiseId);

    /**
     * Reemplaza una franquicia existente.
     */
    Mono<Franchise> update(Franchise franchise);
}
