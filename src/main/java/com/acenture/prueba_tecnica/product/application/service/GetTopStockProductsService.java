package com.acenture.prueba_tecnica.product.application.service;

import com.acenture.prueba_tecnica.branch.application.port.out.BranchRepositoryPort;
import com.acenture.prueba_tecnica.branch.domain.model.Branch;
import com.acenture.prueba_tecnica.franchise.application.port.out.FranchiseRepositoryPort;
import com.acenture.prueba_tecnica.product.application.port.in.GetTopStockProductsUseCase;
import com.acenture.prueba_tecnica.product.application.port.out.ProductRepositoryPort;
import com.acenture.prueba_tecnica.product.domain.model.BranchTopStockProduct;
import com.acenture.prueba_tecnica.product.domain.model.Product;
import com.acenture.prueba_tecnica.shared.domain.exception.ResourceNotFoundException;
import reactor.core.publisher.Flux;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

/**
 * Caso de uso: producto con más stock por sucursal.
 * Reglas:
 *  - Una sucursal sin productos no aparece en el resultado.
 *  - Si hay empate en stock, gana el producto con nombre alfabéticamente menor.
 *  - El resultado se ordena por nombre de sucursal.
 */
public class GetTopStockProductsService implements GetTopStockProductsUseCase {

    private static final Comparator<BranchTopStockProduct> BY_BRANCH_NAME = Comparator
            .comparing(BranchTopStockProduct::branchName)
            .thenComparing(BranchTopStockProduct::branchId);

    private final FranchiseRepositoryPort franchiseRepository;
    private final BranchRepositoryPort branchRepository;
    private final ProductRepositoryPort productRepository;

    public GetTopStockProductsService(FranchiseRepositoryPort franchiseRepository,
                                      BranchRepositoryPort branchRepository,
                                      ProductRepositoryPort productRepository) {
        this.franchiseRepository = Objects.requireNonNull(franchiseRepository);
        this.branchRepository = Objects.requireNonNull(branchRepository);
        this.productRepository = Objects.requireNonNull(productRepository);
    }

    @Override
    public Flux<BranchTopStockProduct> getByFranchise(String franchiseId) {
        return franchiseRepository.existsById(franchiseId)
                .flatMapMany(exists -> exists
                        ? topProductPerBranch(franchiseId)
                        : Flux.error(new ResourceNotFoundException("La franquicia " + franchiseId + " no existe")));
    }

    private Flux<BranchTopStockProduct> topProductPerBranch(String franchiseId) {
        return branchRepository.findByFranchiseId(franchiseId)
                .collectMap(Branch::id, Branch::name)
                .flatMapMany(branchNames -> productRepository.findByFranchiseId(franchiseId)
                        .filter(product -> branchNames.containsKey(product.branchId()))
                        .groupBy(Product::branchId)
                        .flatMap(productsOfBranch -> productsOfBranch.reduce(Product::higherStock))
                        .map(top -> toResult(top, branchNames)))
                .sort(BY_BRANCH_NAME);
    }

    private static BranchTopStockProduct toResult(Product product, Map<String, String> branchNames) {
        return new BranchTopStockProduct(product.branchId(), branchNames.get(product.branchId()),
                product.id(), product.name(), product.stock());
    }
}
