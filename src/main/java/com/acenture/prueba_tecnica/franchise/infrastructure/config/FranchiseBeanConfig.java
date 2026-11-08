package com.acenture.prueba_tecnica.franchise.infrastructure.config;

import com.acenture.prueba_tecnica.franchise.application.port.in.CreateFranchiseUseCase;
import com.acenture.prueba_tecnica.franchise.application.port.out.FranchiseRepositoryPort;
import com.acenture.prueba_tecnica.franchise.application.service.CreateFranchiseService;
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
}
