package com.acenture.prueba_tecnica.franchise.infrastructure.in.web.dto;

import com.acenture.prueba_tecnica.franchise.domain.model.Franchise;

public record FranchiseResponse(String id, String name) {

    public static FranchiseResponse from(Franchise franchise) {
        return new FranchiseResponse(franchise.id(), franchise.name());
    }
}
