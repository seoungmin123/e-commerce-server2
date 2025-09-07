package kr.hhplus.be.server.domain.service.integration;


import kr.hhplus.be.server.DataBaseCleanUp;
import kr.hhplus.be.server.ServerApplication;
import kr.hhplus.be.server.common.exception.ApiErrorCode;
import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.domain.coupon.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.domain.CouponIssueProcessor;
import kr.hhplus.be.server.domain.coupon.domain.ICouponRepository;
import kr.hhplus.be.server.domain.coupon.dto.CouponCommand;
import kr.hhplus.be.server.domain.coupon.dto.CouponInfo;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.user.domain.IUserRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static kr.hhplus.be.server.common.exception.ApiErrorCode.INSUFFICIENT_COUPON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(classes = ServerApplication.class)
@Testcontainers
class CouponServiceIntegrationTest {
    @Autowired
    private CouponService couponService;
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private DataBaseCleanUp dataBaseCleanUp;
    @Autowired
    private ICouponRepository couponRepository;
    @Autowired
    private CouponIssueProcessor couponIssueProcessor;
    @Autowired
    private RedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        dataBaseCleanUp.execute();
        // Redis 데이터 초기화
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.flushDb();
            return null;
        });
    }

    @Test
    void 쿠폰_발급이_정상적으로_동작한다() {
        // given
        Long userId = 1L;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));

        // when
        CouponInfo couponInfo = couponService.issueCoupon(new CouponCommand.Issue(user,1L));

        // then
        assertThat(couponInfo.couponId()).isEqualTo(1L);
        assertThat(couponInfo.discountType()).isEqualTo("정률");
        assertThat(couponInfo.discountAmount()).isEqualTo(BigDecimal.valueOf(10).setScale(2));
        assertThat(couponInfo.status()).isEqualTo("미사용");

    }


    @Test
    void 동시에_쿠폰발급시_발급수량을_초과하면_INSUFFICIENT_COUPON_예외가_발생한다() throws InterruptedException {
        // given
        int threadCount = 5;
        Long couponId = 2L;   // 최대 발급수량이 '3'인 쿠폰
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            Long userId = (long) (i + 1);  // 각각 다른 사용자 userId = 2부터 5명
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));

            executorService.submit(() -> {
                try {
                    couponService.issueCoupon(new CouponCommand.Issue(user, couponId));
                    successCount.incrementAndGet();
                } catch (ApiException e) {
                    if (e.getApiErrorCode() == INSUFFICIENT_COUPON) {
                        failCount.incrementAndGet();
                    }else{
                        throw e;
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // then
        latch.await(5, TimeUnit.SECONDS);
        assertThat(successCount.get()).isEqualTo(3);  // 발급 가능 수량
        assertThat(failCount.get()).isEqualTo(2);     // 초과 요청 수
    }

    @Test
    void 쿠폰목록_조회시_전체_보유_쿠폰목록이_조회된다() {
        // given
        Long userId = 1L;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));

        // when
        List<CouponInfo> coupons = couponService.getCoupons(user);

        // then
        AssertionsForInterfaceTypes.assertThat(coupons)
                .hasSize(2)
                .element(0)
                .satisfies(coupon -> {
                    AssertionsForClassTypes.assertThat(coupon.status()).isEqualTo("사용 완료");
                    AssertionsForClassTypes.assertThat(coupon.discountType()).isEqualTo("정률");
                    AssertionsForClassTypes.assertThat(coupon.discountAmount()).isEqualTo(BigDecimal.valueOf(10).setScale(2));
                    AssertionsForClassTypes.assertThat(coupon.usedAt()).isNotNull();
                });
    }


    @Test
    void 동일유저가_이미_발급받은_쿠폰을_재발급시_CONFLICT_예외가_발생한다() {
        // given
        Long userId = 1L;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));
        Long couponId = 1L;
        CouponCommand.Issue command = new CouponCommand.Issue(user,couponId);

        // when
        // 첫 번째 발급 시도
        couponService.issueCoupon(command);

        // then
        // 두 번째 발급 시도시 예외 발생
        assertThatThrownBy(() ->
                couponService.issueCoupon(command)
        )
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiErrorCode", ApiErrorCode.CONFLICT);
    }

    @Test
    void 쿠폰발급_요청시_성공하면_요청이력에_저장되고_발급이력에는_저장되지_않는다() {
        // given
        Long userId = 1L;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));
        Long couponId = 5L;
        CouponCommand.Issue command = new CouponCommand.Issue(user, couponId);

        // when
        boolean result = couponService.requestConponIssue(command);


        // then
        assertThat(result).isTrue();
        assertThat(couponRepository.getRequestCount(couponId)).isEqualTo(1);
        assertThat(couponRepository.isIssuedMember(couponId, userId)).isFalse();
    }

    @Test
    void 동일_사용자가_여러번_요청시_한번만_요청이력에_저장된다() {
        // given
        Long userId = 1L;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));
        Long couponId = 5L;
        CouponCommand.Issue command = new CouponCommand.Issue(user, couponId);

        // when
        couponService.requestConponIssue(command);
        couponService.requestConponIssue(command);
        couponService.requestConponIssue(command);

        // then
        assertThat(couponRepository.getRequestCount(couponId)).isEqualTo(1);
        assertThat(couponRepository.isIssuedMember(couponId, userId)).isFalse();
    }

    @Test
    void 쿠폰발급_요청시_이미_발급받은_요청이면_CONFLICT_예외가_발생한다() {
        // given
        Long userId = 1L;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));
        Long couponId = 5L;
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));
        CouponCommand.Issue command = new CouponCommand.Issue(user, couponId);


        // 먼저 한 번 요청 후 발급처리
        couponService.requestConponIssue(command);
        couponIssueProcessor.processCouponIssuance(coupon);

        // when & then
        assertThatThrownBy(() ->
                couponService.requestConponIssue(command)
        ).isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiErrorCode", ApiErrorCode.CONFLICT);
        assertThat(couponRepository.getRequestCount(couponId)).isEqualTo(0); // 요청이력에 저장되지 않음
    }
}