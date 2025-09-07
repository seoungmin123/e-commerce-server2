package kr.hhplus.be.server.domain.product.domain;

import kr.hhplus.be.server.domain.order.domain.IOrderRepository;
import kr.hhplus.be.server.domain.product.dto.PopularProductInfo;
import kr.hhplus.be.server.domain.product.dto.PopularProductQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class PopularProductCacheManager {
    private final IOrderRepository orderRepository;
    private final IProductRepository productRepository;

    public void refreshPopularProducts() {
        try {
            List<PopularProductQuery> products = orderRepository.findTopFivePopularProducts();
            productRepository.addToTempKeys(products); // 새로운 정보 임시 키에 저장
            switchKeys();
        } catch (Exception e) {
            log.error("Failed to refresh popular products cache", e);
            productRepository.deleteKeys(
                    productRepository.getTempSortedKey(),
                    productRepository.getTempHashKey()
            );
            throw e;
        }
    }

    private void switchWithBackup() {
        productRepository.moveKeys(
                productRepository.getCurrentSortedKey(),
                productRepository.getBackupSortedKey(),
                productRepository.getCurrentHashKey(),
                productRepository.getBackupHashKey()
        );

        productRepository.moveKeys(
                productRepository.getTempSortedKey(),
                productRepository.getCurrentSortedKey(),
                productRepository.getTempHashKey(),
                productRepository.getCurrentHashKey()
        );

//        productRepository.deleteKeys(
//                productRepository.getBackupSortedKey(),
//                productRepository.getBackupHashKey()
//        );

        // 삭제 대신 TTL 설정 (30분)
        productRepository.expireBackupKeys(Duration.ofMinutes(30));
    }

    private boolean isTempReady() {
        return productRepository.existsKey(productRepository.getTempSortedKey()) &&
                productRepository.existsKey(productRepository.getTempHashKey());
    }

    private void switchKeys() {
        if (!isTempReady()) {
            log.warn("switchKeys: TEMP snapshot not ready. Skip switching to preserve CURRENT.");
            // TEMP 찌꺼기 삭제(있다면)
            productRepository.deleteKeys(
                    productRepository.getTempSortedKey(),
                    productRepository.getTempHashKey()
            );
            return;
        }

        if (productRepository.existsKey(productRepository.getCurrentSortedKey())) {
            switchWithBackup(); // 현재 키가 존재하면 백업에 저장하고 임시키를 현재키로 변경
        } else {
            productRepository.moveKeys(
                    productRepository.getTempSortedKey(),
                    productRepository.getCurrentSortedKey(),
                    productRepository.getTempHashKey(),
                    productRepository.getCurrentHashKey()
            );
        }
    }

    public List<PopularProductInfo> getTopProducts(int limit) {
        Set<ZSetOperations.TypedTuple<Long>> topProducts =
                productRepository.getTopProductIds(productRepository.getCurrentSortedKey(), limit);
        String hashKey = productRepository.getCurrentHashKey();

        // 현재 키에 인기 상품이 없으면 백업키에서 가져옴
        if (topProducts == null || topProducts.isEmpty()) {
            topProducts = productRepository.getTopProductIds(productRepository.getBackupSortedKey(), limit);
            hashKey = productRepository.getBackupHashKey();
        }
        // 그래도 없으면 빈 리스트 반환
        if (topProducts == null || topProducts.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. ZSET 멤버(Long productId) 꺼냄 →
        // 2. 해당 productId들로 HASH 멀티겟 →
        // 3. JSON → DTO
        List<String> productIds = topProducts.stream()
                .map(tuple -> String.valueOf(tuple.getValue()))
                .toList();

        return productRepository.getProductHashValues(hashKey, productIds)
                .stream()
                .map(PopularProductInfo::from)
                .toList();
    }
}