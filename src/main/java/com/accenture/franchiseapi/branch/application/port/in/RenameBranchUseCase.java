package com.accenture.franchiseapi.branch.application.port.in;

import com.accenture.franchiseapi.branch.domain.model.Branch;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada: cambiar el nombre de una sucursal.
 */
public interface RenameBranchUseCase {

    Mono<Branch> rename(RenameBranchCommand command);

    record RenameBranchCommand(String franchiseId, String branchId, String name) {
    }
}
