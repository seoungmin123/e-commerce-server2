package kr.hhplus.be.server.coupon;


import kr.hhplus.be.server.common.exception.ApiErrorCode;
import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.coupon.domain.Coupon;
import kr.hhplus.be.server.coupon.domain.CouponIssue;
import kr.hhplus.be.server.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static kr.hhplus.be.server.coupon.domain.CouponIssue.CouponStatus.*;
import static org.assertj.core.api.Assertions.*;

class CouponIssueTest {

    @Nested
    @DisplayName("쿠폰 발급 테스트")
    class createCouponIssueTest {
        @Test
        void 쿠폰발급_생성시_만료일이_유효기간만큼_설정된다() {
            // given
            User user = User.create("테스트유저");
            Coupon coupon = Coupon.create("테스트쿠폰", Coupon.DiscountType.FIXED,
                    BigDecimal.valueOf(1000),
                    LocalDateTime.now(), LocalDateTime.now().plusDays(30), 7,50);

            // when
            CouponIssue couponIssue = CouponIssue.create(user, coupon);

            // then
            assertThat(couponIssue.getExpiredAt())
                    .isCloseTo(LocalDateTime.now().plusDays(7), within(1, ChronoUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("쿠폰사용")
    class useCouponIssueTest {
        @Test
        void 쿠폰사용검증시_이미_사용된_쿠폰이면_INVALID_REQUEST_예외가_발생한다() {
            // given
            User user = User.create("테스트 유저");
            ReflectionTestUtils.setField(user, "id", 1L);
            CouponIssue couponIssue = createCouponIssue();
            ReflectionTestUtils.setField(couponIssue, "usedAt", LocalDateTime.now());

            // when & then
            assertThatThrownBy(() -> couponIssue.validateUseable(user))
                    .isInstanceOf(ApiException.class)
                    .extracting("apiErrorCode")
                    .isEqualTo(ApiErrorCode.INVALID_REQUEST);
        }

        @Test
        void 쿠폰검증시_만료된_쿠폰이면_INVALID_REQUEST_예외가_발생한다() {
            // given
            User user = User.create("테스트 유저");
            ReflectionTestUtils.setField(user, "id", 1L);
            CouponIssue couponIssue = createCouponIssue();
            ReflectionTestUtils.setField(couponIssue, "expiredAt", LocalDateTime.now().minusDays(1));

            // when & then
            assertThatThrownBy(() -> couponIssue.validateUseable(user))
                    .isInstanceOf(ApiException.class)
                    .extracting("apiErrorCode")
                    .isEqualTo(ApiErrorCode.INVALID_REQUEST);
        }

        @Test
        void 할인금액계산시_정액할인_쿠폰이면_설정된_금액만큼_할인된다() {
            // given
            CouponIssue couponIssue = createFixedCouponIssue(BigDecimal.valueOf(1000));
            BigDecimal orderAmount = BigDecimal.valueOf(10000);

            // when
            BigDecimal discountAmount = couponIssue.calculateDiscountAmount(orderAmount);

            // then
            assertThat(discountAmount).isEqualTo(BigDecimal.valueOf(1000));
        }

        @Test
        void 할인금액계산시_정률할인_쿠폰이면_주문금액기준_할인율만큼_할인된다() {
            // given
            CouponIssue couponIssue = createPercentageCouponIssue(BigDecimal.valueOf(10));
            BigDecimal orderAmount = BigDecimal.valueOf(10000);

            // when
            BigDecimal discountAmount = couponIssue.calculateDiscountAmount(orderAmount);

            // then
            assertThat(discountAmount).isEqualTo(BigDecimal.valueOf(1000));
        }

        @Test
        void 쿠폰상태_조회시_상황에_따라_적절한_상태가_반환된다() {
            // given
            User user = User.create("테스트유저");
            ReflectionTestUtils.setField(user, "id", 1L);
            CouponIssue unusedCoupon = createCouponIssue();
            ReflectionTestUtils.setField(unusedCoupon, "expiredAt", LocalDateTime.now().plusDays(1));

            CouponIssue expiredCoupon = createCouponIssue();
            ReflectionTestUtils.setField(expiredCoupon, "expiredAt", LocalDateTime.now().minusDays(1));

            CouponIssue usedCoupon = createCouponIssue();
            ReflectionTestUtils.setField(usedCoupon, "user", user);
            usedCoupon.use(user);

            // when & then
            assertThat(unusedCoupon.getStatus()).isEqualTo(UNUSED.getDescription());
            assertThat(expiredCoupon.getStatus()).isEqualTo(EXPIRED.getDescription());
            assertThat(usedCoupon.getStatus()).isEqualTo(USED.getDescription());
        }


        private CouponIssue createCouponIssue() {
            User user = User.create("테스트유저");
            Coupon coupon = Coupon.create("테스트쿠폰", Coupon.DiscountType.FIXED,
                    BigDecimal.valueOf(1000),
                    LocalDateTime.now(), LocalDateTime.now().plusDays(30), 7, 30);
            return CouponIssue.create(user, coupon);
        }

        private CouponIssue createFixedCouponIssue(BigDecimal discountAmount) {
            User user = User.create("테스트유저");
            Coupon coupon = Coupon.create("정액할인쿠폰", Coupon.DiscountType.FIXED,
                    discountAmount,
                    LocalDateTime.now(), LocalDateTime.now().plusDays(30), 7, 30);
            return CouponIssue.create(user, coupon);
        }

        private CouponIssue createPercentageCouponIssue(BigDecimal discountRate) {
            User user = User.create("테스트유저");
            Coupon coupon = Coupon.create("정률할인쿠폰", Coupon.DiscountType.PERCENTAGE,
                    discountRate,
                    LocalDateTime.now(), LocalDateTime.now().plusDays(30), 7, 30);
            return CouponIssue.create(user, coupon);
        }

        private CouponIssue createCouponIssueWithMinimumOrderAmount(BigDecimal minimumOrderAmount) {
            User user = User.create("테스트유저");
            Coupon coupon = Coupon.create("최소주문금액쿠폰", Coupon.DiscountType.FIXED,
                    BigDecimal.valueOf(1000),
                    LocalDateTime.now(), LocalDateTime.now().plusDays(30), 7, 30);
            return CouponIssue.create(user, coupon);
        }
    }
}