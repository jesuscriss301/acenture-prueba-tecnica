package com.acenture.prueba_tecnica.branch.infrastructure.config;

import com.acenture.prueba_tecnica.branch.application.port.in.AddBranchUseCase;
import com.acenture.prueba_tecnica.branch.application.port.out.BranchRepositoryPort;
import com.acenture.prueba_tecnica.branch.application.service.AddBranchService;
import com.acenture.prueba_tecnica.franchise.application.port.out.FranchiseRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BranchBeanConfig {

    @Bean
    public AddBranchUseCase addBranchUseCase(FranchiseRepositoryPort franchiseRepository,
                                             BranchRepositoryPort branchRepository) {
        return new AddBranchService(franchiseRepository, branchRepository);
    }
}
