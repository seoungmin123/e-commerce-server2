package kr.hhplus.be.server.infra;


import kr.hhplus.be.server.infra.product.ProductCacheRepository;
import kr.hhplus.be.server.domain.product.domain.PopularProductCacheManager;
import kr.hhplus.be.server.domain.product.dto.PopularProductInfo;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Sql(scripts = {"/cleanup.sql", "/test-data.sql"})
class PopularProductCacheManagerIntegrationTest {
    @Autowired
    private PopularProductCacheManager cacheManager;

    @Autowired
    private ProductCacheRepository cacheRepository;

    @BeforeEach
    void setUp() {
        cacheRepository.deleteKeys(
                cacheRepository.getCurrentSortedKey(),
                cacheRepository.getBackupSortedKey(),
                cacheRepository.getTempSortedKey(),
                cacheRepository.getCurrentHashKey(),
                cacheRepository.getBackupHashKey(),
                cacheRepository.getTempHashKey()
        );
    }

    @Test
    void 인기상품_갱신시_데이터가_정상적으로_저장된다() {
        // when
        cacheManager.refreshPopularProducts();

        // then
        Set<ZSetOperations.TypedTuple<Long>> result =
                cacheRepository.getTopProductIds(cacheRepository.getCurrentSortedKey(), 5);
        assertThat(result).hasSize(2);

        List<Long> orderedIds = result.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .collect(Collectors.toList());
        assertThat(orderedIds).containsExactly(1L, 2L);
    }

    @Test
    void 인기상품_조회시_현재키가_없으면_백업키에서_조회된다() {
        // given
        cacheManager.refreshPopularProducts();
        cacheRepository.moveKeys(
                cacheRepository.getCurrentSortedKey(),
                cacheRepository.getBackupSortedKey(),
                cacheRepository.getCurrentHashKey(),
                cacheRepository.getBackupHashKey()
        );

        // when
        List<PopularProductInfo> result = cacheManager.getTopProducts(5);

        // then
        assertThat(result).hasSize(2);
        AssertionsForClassTypes.assertThat(result.get(0).productId()).isEqualTo(1L);
    }
}