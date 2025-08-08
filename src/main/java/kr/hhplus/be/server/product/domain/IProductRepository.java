package kr.hhplus.be.server.product.domain;

import java.util.List;
import java.util.Optional;

public interface IProductRepository {
    List<Product> findAll();

    Optional<Product> findById(Long id);

    Optional<Product> findByIdWithStock(Long aLong);

    Product save(Product product);

    Optional<ProductStock> findByIdWithLock(Long id);

    ProductStock save(ProductStock productStock);

    List<Product> findAllById(List<Long> productIds);

    //비관적락 적용
    List<ProductStock> findAllByIdsWithLock(List<Long> productIds);

    List<ProductStock> saveAll(List<ProductStock> stocks);

    Optional<ProductStock> findByProductId(Long productId);

    List<ProductStock> findAllByProductIds(List<Long> productIds);
}
