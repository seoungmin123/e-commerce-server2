package kr.hhplus.be.server.infra;


import kr.hhplus.be.server.infra.product.ProductCacheRepository;
import kr.hhplus.be.server.domain.product.dto.PopularProductInfo;
import kr.hhplus.be.server.domain.product.dto.PopularProductQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ZSetOperations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductCacheRepositoryTest {
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
    void 임시키에_데이터_저장시_정렬순서가_유지된다() {
        // given
        List<PopularProductQuery> products = List.of(
                new PopularProductQuery(1L, "상품1", BigDecimal.valueOf(1000), 50),
                new PopularProductQuery(2L, "상품2", BigDecimal.valueOf(2000), 100),
                new PopularProductQuery(3L, "상품3", BigDecimal.valueOf(3000), 75)
        );

        // when
        cacheRepository.addToTempKeys(products);

        // then
        Set<ZSetOperations.TypedTuple<Long>> result =
                cacheRepository.getTopProductIds(cacheRepository.getTempSortedKey(), 3);

        List<Long> sortedIds = result.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .collect(Collectors.toList());

        assertThat(sortedIds).containsExactly(2L, 3L, 1L); // 판매량 순
    }

    @Test
    void 키_이동시_데이터가_정상적으로_이동된다() {
        // given
        List<PopularProductQuery> products = List.of(
                new PopularProductQuery(1L, "상품1", BigDecimal.valueOf(1000), 100)
        );
        cacheRepository.addToTempKeys(products);

        // when
        cacheRepository.moveKeys(
                cacheRepository.getTempSortedKey(),
                cacheRepository.getCurrentSortedKey(),
                cacheRepository.getTempHashKey(),
                cacheRepository.getCurrentHashKey()
        );

        // then
        assertThat(cacheRepository.existsKey(cacheRepository.getTempSortedKey())).isFalse();
        assertThat(cacheRepository.existsKey(cacheRepository.getTempHashKey())).isFalse();
        assertThat(cacheRepository.existsKey(cacheRepository.getCurrentSortedKey())).isTrue();
        assertThat(cacheRepository.existsKey(cacheRepository.getCurrentHashKey())).isTrue();
    }

    @Test
    void 해시데이터_조회시_여러개의_상품정보가_정상적으로_조회된다() {
        // given
        List<PopularProductQuery> products = List.of(
                new PopularProductQuery(1L, "상품1", BigDecimal.valueOf(1000), 100),
                new PopularProductQuery(2L, "상품2", BigDecimal.valueOf(2000), 200)
        );
        cacheRepository.addToTempKeys(products);
        cacheRepository.moveKeys(
                cacheRepository.getTempSortedKey(),
                cacheRepository.getCurrentSortedKey(),
                cacheRepository.getTempHashKey(),
                cacheRepository.getCurrentHashKey()
        );

        // when
        List<String> hashValues = cacheRepository.getProductHashValues(
                cacheRepository.getCurrentHashKey(),
                List.of("1", "2")
        );

        // then
        assertThat(hashValues).hasSize(2);
        PopularProductInfo info1 = PopularProductInfo.from(hashValues.get(0));
        PopularProductInfo info2 = PopularProductInfo.from(hashValues.get(1));
        assertThat(info1.name()).isEqualTo("상품1");
        assertThat(info2.name()).isEqualTo("상품2");
    }
}