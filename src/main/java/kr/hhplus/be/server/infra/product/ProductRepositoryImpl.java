package kr.hhplus.be.server.infra.product;


import kr.hhplus.be.server.domain.product.domain.IProductRepository;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.product.domain.ProductStock;
import kr.hhplus.be.server.domain.product.dto.PopularProductQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements IProductRepository {
    private final ProductJpaRepository productJpaRepository;
    private final ProductStockJpaRepository productStockJpaRepository;
    private final ProductCacheRepository productCacheRepository;

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
    @Override
    public void addToTempKeys(List<PopularProductQuery> products) {
        productCacheRepository.addToTempKeys(products);
    }

    @Override
    public void moveKeys(String oldSortedKey, String newSortedKey, String oldHashKey, String newHashKey) {
        productCacheRepository.moveKeys(oldSortedKey, newSortedKey, oldHashKey, newHashKey);
    }

    @Override
    public void deleteKeys(String... keys) {
        productCacheRepository.deleteKeys(keys);
    }

    @Override
    public boolean existsKey(String key) {
        return productCacheRepository.existsKey(key);
    }

    @Override
    public String getTempSortedKey() {
        return productCacheRepository.getTempSortedKey();
    }

    @Override
    public String getCurrentSortedKey() {
        return productCacheRepository.getCurrentSortedKey();
    }

    @Override
    public String getBackupSortedKey() {
        return productCacheRepository.getBackupSortedKey();
    }

    @Override
    public String getTempHashKey() {
        return productCacheRepository.getTempHashKey();
    }

    @Override
    public String getCurrentHashKey() {
        return productCacheRepository.getCurrentHashKey();
    }

    @Override
    public String getBackupHashKey() {
        return productCacheRepository.getBackupHashKey();
    }

    @Override
    public List<String> getProductHashValues(String hashKey, List<String> productIds) {
        return productCacheRepository.getProductHashValues(hashKey, productIds);
    }

    @Override
    public Set<ZSetOperations.TypedTuple<Long>> getTopProductIds(String sortedKey, int limit) {
        return productCacheRepository.getTopProductIds(sortedKey, limit);
    }

    @Override
    public boolean expire(String key, Duration ttl) {
        return productCacheRepository.expire(key, ttl);
    }

    @Override
    public void expireBackupKeys(Duration ttl) {
        productCacheRepository.expireBackupKeys(ttl);
    }


}
