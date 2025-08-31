package kr.hhplus.be.server.domain.product.domain;

import kr.hhplus.be.server.domain.product.dto.PopularProductQuery;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    void addToTempKeys(List<PopularProductQuery> products);

    String getCurrentSortedKey();

    void moveKeys(String oldSortedKey, String newSortedKey, String oldHashKey, String newHashKey);

    void deleteKeys(String... keys);

    boolean existsKey(String key);

    String getTempSortedKey();

    String getBackupSortedKey();

    String getTempHashKey();

    String getCurrentHashKey();

    String getBackupHashKey();

    List<String> getProductHashValues(String hashKey, List<String> productIds);

    Set<ZSetOperations.TypedTuple<Long>> getTopProductIds(String sortedKey, int limit);

    boolean expire(String key, Duration ttl);

    void expireBackupKeys(Duration ttl);
}
