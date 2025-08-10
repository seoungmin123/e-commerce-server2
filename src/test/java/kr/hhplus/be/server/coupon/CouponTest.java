package kr.hhplus.be.server.coupon;

import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.coupon.domain.Coupon;
import kr.hhplus.be.server.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static kr.hhplus.be.server.common.exception.ApiErrorCode.INSUFFICIENT_COUPON;
import static kr.hhplus.be.server.common.exception.ApiErrorCode.INVALID_REQUEST;
import static org.assertj.core.api.Assertions.*;
class CouponTest {

    @Test
    void 쿠폰_발급시_발급_가능_기간이_아니면_INVALID_REQUEST_예외가_발생한다(){
        // given
        User user = User.create("테스트유저");
        Coupon coupon = Coupon.create(
                "테스트쿠폰",
                Coupon.DiscountType.FIXED,
                BigDecimal.valueOf(1000),
                LocalDateTime.now().plusDays(1),  // 발급 시작일이 내일
                LocalDateTime.now().plusDays(7),
                30,
                100
        );
        int beforeQuantity = coupon.getIssuedQuantity();

        // when & then
        assertThatThrownBy(() -> coupon.issue(user))
                .isInstanceOf(ApiException.class)
                .extracting("apiErrorCode")
                .isEqualTo(INVALID_REQUEST);

        assertThat(coupon.getIssuedQuantity()).isEqualTo(beforeQuantity);
    }
    @Test
    void 쿠폰_발급시_발급_가능_수량이_0이면_INSUFFICIENT_COUPON_예외가_발생한다(){
        // given
        User user = User.create("테스트유저");
        ReflectionTestUtils.setField(user, "id", 1L);
        User user2 = User.create("테스트유저2");
        ReflectionTestUtils.setField(user, "id", 2L);
        Coupon coupon = Coupon.create(
                "테스트쿠폰",
                Coupon.DiscountType.FIXED,
                BigDecimal.valueOf(1000),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(7),
                30,
                1  // 최대 발급 수량 1개
        );

        coupon.issue(user);  // 쿠폰 1개 발급
        int beforeQuantity = coupon.getIssuedQuantity();

        // when & then
        assertThatThrownBy(() -> coupon.issue(user2))
                .isInstanceOf(ApiException.class)
                .extracting("apiErrorCode")
                .isEqualTo(INSUFFICIENT_COUPON);

        assertThat(coupon.getIssuedQuantity()).isEqualTo(beforeQuantity);
    }
}