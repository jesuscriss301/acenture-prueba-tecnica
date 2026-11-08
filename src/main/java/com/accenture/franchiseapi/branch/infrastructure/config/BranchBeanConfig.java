package com.accenture.franchiseapi.branch.infrastructure.config;

import com.accenture.franchiseapi.branch.application.port.in.AddBranchUseCase;
import com.accenture.franchiseapi.branch.application.port.in.RenameBranchUseCase;
import com.accenture.franchiseapi.branch.application.port.out.BranchRepositoryPort;
import com.accenture.franchiseapi.branch.application.service.AddBranchService;
import com.accenture.franchiseapi.branch.application.service.RenameBranchService;
import com.accenture.franchiseapi.franchise.application.port.out.FranchiseRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BranchBeanConfig {

    @Bean
    public AddBranchUseCase addBranchUseCase(FranchiseRepositoryPort franchiseRepository,
                                             BranchRepositoryPort branchRepository) {
        return new AddBranchService(franchiseRepository, branchRepository);
    }

    @Bean
    public RenameBranchUseCase renameBranchUseCase(BranchRepositoryPort branchRepository) {
        return new RenameBranchService(branchRepository);
    }
}
