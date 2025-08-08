package kr.hhplus.be.server.infra.product;


import kr.hhplus.be.server.product.domain.IProductRepository;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.ProductStock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements IProductRepository {
    private final ProductJpaRepository productJpaRepository;
    private final ProductStockJpaRepository productStockJpaRepository;


    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAll();
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id);
    }

    // 비관적락 적용
    @Override
    public Optional<Product> findByIdWithStock(Long id) {
        return productJpaRepository.findByIdWithStock(id);
    }

    //비관적락 적용
    @Override
    public Optional<ProductStock> findByIdWithLock(Long id) {
        return productStockJpaRepository.findByIdWithLock(id);
    }

    @Override
    public List<ProductStock> findAllByIdsWithLock(List<Long> productIds) {
        return productStockJpaRepository.findAllByIdsWithLock(productIds);
    }

    @Override
    public ProductStock save(ProductStock productStock) {
        return productStockJpaRepository.save(productStock);
    }

    @Override
    public List<ProductStock> saveAll(List<ProductStock> stocks) {
        return productStockJpaRepository.saveAll(stocks);
    }

    @Override
    public List<Product> findAllById(List<Long> productIds) {
        return productJpaRepository.findAllById(productIds);
    }

    @Override
    public Optional<ProductStock> findByProductId(Long productId) {
        return productStockJpaRepository.findByProductId(productId);
    }

    @Override
    public List<ProductStock> findAllByProductIds(List<Long> productIds) {
        return productStockJpaRepository.findAllByProductIds(productIds);
    }



}
