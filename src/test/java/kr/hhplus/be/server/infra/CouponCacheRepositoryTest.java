package kr.hhplus.be.server.infra;


import kr.hhplus.be.server.infra.coupon.CouponCacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@SpringBootTest
class CouponCacheRepositoryTest {
    @Autowired
    private CouponCacheRepository couponCacheRepository;

    @Autowired
    private RedisTemplate<String, Long> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.flushDb();
            return null;
        });

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void 쿠폰발급요청시_요청순서대로_정렬되어_저장된다() {
        // given
        Long couponId = 5L;

        // when
        couponCacheRepository.addRequest(couponId, 3L);
        couponCacheRepository.addRequest(couponId, 1L);
        couponCacheRepository.addRequest(couponId, 2L);

        // then
        Set<ZSetOperations.TypedTuple<Long>> requests =
                couponCacheRepository.getRequestsByTimestamp(couponId, 3);

        List<Long> userIds = requests.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .collect(Collectors.toList());

        assertThat(userIds).containsExactly(3L, 1L, 2L);

        // timestamp 정렬 확인
        List<Double> timestamps = requests.stream()
                .map(ZSetOperations.TypedTuple::getScore)
                .collect(Collectors.toList());

        assertThat(timestamps).isSorted();
    }

    @Test
    void 쿠폰발급요청시_동일유저가_여러번요청해도_한번만_저장된다() {
        // given
        Long couponId = 5L;
        Long userId = 1L;

        // when
        boolean firstResult = couponCacheRepository.addRequest(couponId, userId);
        boolean secondResult = couponCacheRepository.addRequest(couponId, userId);
        boolean thirdResult = couponCacheRepository.addRequest(couponId, userId);

        // then
        assertThat(firstResult).isTrue();
        assertThat(secondResult).isFalse();
        assertThat(thirdResult).isFalse();
        assertThat(couponCacheRepository.getRequestCount(couponId)).isEqualTo(1);
    }

    @Test
    void 쿠폰발급성공시_발급이력에_추가된다() {
        // given
        Long couponId = 5L;
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);

        // when
        couponCacheRepository.addIssuance(couponId, userIds);

        // then
        assertThat(couponCacheRepository.getIssuedCount(couponId)).isEqualTo(3);
        userIds.forEach(userId ->
                assertThat(couponCacheRepository.isMember(couponId, userId)).isTrue()
        );
    }

    @Test
    void 쿠폰발급실패시_실패이력에_추가된다() {
        // given
        Long couponId = 5L;
        List<Long> failedUserIds = Arrays.asList(1L, 2L);

        // when
        couponCacheRepository.addFailures(couponId, failedUserIds);

        // then
        assertThat(couponCacheRepository.getFailedCount(couponId)).isEqualTo(2);
    }

    @Test
    void 쿠폰요청처리후_해당요청들이_제거된다() {
        // given
        Long couponId = 5L;
        couponCacheRepository.addRequest(couponId, 1L);
        couponCacheRepository.addRequest(couponId, 2L);
        couponCacheRepository.addRequest(couponId, 3L);
        int initialCount = couponCacheRepository.getRequestCount(couponId);

        // when
        couponCacheRepository.removeRequests(couponId, 2);  // 앞의 2개만 제거

        // then
        assertThat(initialCount).isEqualTo(3);
        assertThat(couponCacheRepository.getRequestCount(couponId)).isEqualTo(1);
    }

    @Test
    void 빈리스트_처리시_에러없이_정상처리된다() {
        // given
        Long couponId = 5L;
        List<Long> emptyList = Collections.emptyList();

        // when & then
        assertThatCode(() -> {
            couponCacheRepository.addIssuance(couponId, emptyList);
            couponCacheRepository.addFailures(couponId, emptyList);
        }).doesNotThrowAnyException();
    }

    @Test
    void 동시에_여러요청이_들어와도_모든요청이_요청시간순으로_정렬되어_저장된다() throws InterruptedException {
        // given
        Long couponId = 5L;
        int threadCount = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        // when
        for (long i = 1; i <= threadCount; i++) {
            long userId = i;
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    couponCacheRepository.addRequest(couponId, userId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        // then
        assertThat(couponCacheRepository.getRequestCount(couponId)).isEqualTo(threadCount);

        Set<ZSetOperations.TypedTuple<Long>> requests =
                couponCacheRepository.getRequestsByTimestamp(couponId, threadCount);

        assertThat(requests).hasSize(threadCount);

        List<Double> timestamps = requests.stream()
                .map(ZSetOperations.TypedTuple::getScore)
                .collect(Collectors.toList());

        assertThat(timestamps).isSorted();
    }
}