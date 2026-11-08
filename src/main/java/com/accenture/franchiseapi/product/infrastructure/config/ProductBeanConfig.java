package com.accenture.franchiseapi.product.infrastructure.config;

import com.accenture.franchiseapi.branch.application.port.out.BranchRepositoryPort;
import com.accenture.franchiseapi.franchise.application.port.out.FranchiseRepositoryPort;
import com.accenture.franchiseapi.product.application.port.in.AddProductUseCase;
import com.accenture.franchiseapi.product.application.port.in.DeleteProductUseCase;
import com.accenture.franchiseapi.product.application.port.in.GetTopStockProductsUseCase;
import com.accenture.franchiseapi.product.application.port.in.RenameProductUseCase;
import com.accenture.franchiseapi.product.application.port.in.UpdateProductStockUseCase;
import com.accenture.franchiseapi.product.application.port.out.ProductRepositoryPort;
import com.accenture.franchiseapi.product.application.service.AddProductService;
import com.accenture.franchiseapi.product.application.service.DeleteProductService;
import com.accenture.franchiseapi.product.application.service.GetTopStockProductsService;
import com.accenture.franchiseapi.product.application.service.RenameProductService;
import com.accenture.franchiseapi.product.application.service.UpdateProductStockService;
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

    @Bean
    public GetTopStockProductsUseCase getTopStockProductsUseCase(FranchiseRepositoryPort franchiseRepository,
                                                                 BranchRepositoryPort branchRepository,
                                                                 ProductRepositoryPort productRepository) {
        return new GetTopStockProductsService(franchiseRepository, branchRepository, productRepository);
    }

    @Bean
    public RenameProductUseCase renameProductUseCase(ProductRepositoryPort productRepository) {
        return new RenameProductService(productRepository);
    }
}
