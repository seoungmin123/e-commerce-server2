package kr.hhplus.be.server.infra;


import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.coupon.dto.CouponInfo;
import kr.hhplus.be.server.coupon.dto.CouponIssueCommand;
import kr.hhplus.be.server.coupon.service.CouponService;
import kr.hhplus.be.server.user.domain.IUserRepository;
import kr.hhplus.be.server.user.domain.User;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static kr.hhplus.be.server.common.exception.ApiErrorCode.INSUFFICIENT_COUPON;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Sql(scripts = {"file:./init/01-cleanup.sql", "file:./init/03-test-data.sql"})
class CouponServiceIntegrationTest {
    @Autowired
    private CouponService couponService;
    @Autowired
    private IUserRepository userRepository;


    @Test
    void 쿠폰_발급이_정상적으로_동작한다() {
        // given
        Long userId = 1L;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));

        // when
        CouponInfo couponInfo = couponService.issueCoupon(user, new CouponIssueCommand(1L));

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
                    couponService.issueCoupon(user, new CouponIssueCommand(couponId));
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
}