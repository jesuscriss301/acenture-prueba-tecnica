package com.acenture.prueba_tecnica.product.infrastructure.config;

import com.acenture.prueba_tecnica.branch.application.port.out.BranchRepositoryPort;
import com.acenture.prueba_tecnica.product.application.port.in.AddProductUseCase;
import com.acenture.prueba_tecnica.product.application.port.in.DeleteProductUseCase;
import com.acenture.prueba_tecnica.product.application.port.in.UpdateProductStockUseCase;
import com.acenture.prueba_tecnica.product.application.port.out.ProductRepositoryPort;
import com.acenture.prueba_tecnica.product.application.service.AddProductService;
import com.acenture.prueba_tecnica.product.application.service.DeleteProductService;
import com.acenture.prueba_tecnica.product.application.service.UpdateProductStockService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductBeanConfig {

    @Bean
    public AddProductUseCase addProductUseCase(BranchRepositoryPort branchRepository,
                                               ProductRepositoryPort productRepository) {
        return new AddProductService(branchRepository, productRepository);
    }

    @Bean
    public DeleteProductUseCase deleteProductUseCase(ProductRepositoryPort productRepository) {
        return new DeleteProductService(productRepository);
    }

    @Bean
    public UpdateProductStockUseCase updateProductStockUseCase(ProductRepositoryPort productRepository) {
        return new UpdateProductStockService(productRepository);
    }
}
