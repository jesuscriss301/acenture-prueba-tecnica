package com.accenture.franchiseapi.franchise.infrastructure.config;

import com.accenture.franchiseapi.franchise.application.port.in.CreateFranchiseUseCase;
import com.accenture.franchiseapi.franchise.application.port.in.RenameFranchiseUseCase;
import com.accenture.franchiseapi.franchise.application.port.out.FranchiseRepositoryPort;
import com.accenture.franchiseapi.franchise.application.service.CreateFranchiseService;
import com.accenture.franchiseapi.franchise.application.service.RenameFranchiseService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Conecta los casos de uso (capa de aplicación) con sus adaptadores.
 */
@Configuration
public class FranchiseBeanConfig {

    @Bean
    public CreateFranchiseUseCase createFranchiseUseCase(FranchiseRepositoryPort franchiseRepository) {
        return new CreateFranchiseService(franchiseRepository);
    }

    @Bean
    public RenameFranchiseUseCase renameFranchiseUseCase(FranchiseRepositoryPort franchiseRepository) {
        return new RenameFranchiseService(franchiseRepository);
    }
}
