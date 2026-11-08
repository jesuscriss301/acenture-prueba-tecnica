package com.acenture.prueba_tecnica.branch.infrastructure.in.web.dto;

import com.acenture.prueba_tecnica.branch.domain.model.Branch;

public record BranchResponse(String id, String franchiseId, String name) {

    public static BranchResponse from(Branch branch) {
        return new BranchResponse(branch.id(), branch.franchiseId(), branch.name());
    }
}
