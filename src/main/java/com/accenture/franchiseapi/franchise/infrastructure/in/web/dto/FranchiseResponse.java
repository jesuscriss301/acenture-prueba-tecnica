package com.accenture.franchiseapi.franchise.infrastructure.in.web.dto;

import com.accenture.franchiseapi.franchise.domain.model.Franchise;

public record FranchiseResponse(String id, String name) {

    public static FranchiseResponse from(Franchise franchise) {
        return new FranchiseResponse(franchise.id(), franchise.name());
    }
}
