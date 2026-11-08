package com.acenture.prueba_tecnica.franchise.application.port.out;

import com.acenture.prueba_tecnica.franchise.domain.model.Franchise;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida: persistencia de franquicias.
 * La implementación concreta (DynamoDB) vive en infraestructura.
 */
public interface FranchiseRepositoryPort {

    Mono<Franchise> save(Franchise franchise);

    Mono<Boolean> existsById(String franchiseId);
}
