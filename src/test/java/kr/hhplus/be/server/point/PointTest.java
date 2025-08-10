package kr.hhplus.be.server.point;


import kr.hhplus.be.server.common.exception.ApiErrorCode;
import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.point.domain.Point;
import kr.hhplus.be.server.user.domain.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class PointTest {
    @Test
    void 포인트_생성시_초기값은_0이다() {
        // given
        User user = User.create("테스트유저");

        // when
        Point point = Point.create(user);

        // then
        assertThat(point.getPoint()).isEqualTo(BigDecimal.ZERO);
        assertThat(point.getHistories()).isEmpty();
    }

    @Test
    void 포인트_충전시_충전금액이_최대한도를_초과하면_INVALID_REQUEST_예외가_발생한다() {
        // given
        Point point = Point.create(User.create("테스트유저"));
        BigDecimal overMaxAmount = new BigDecimal("1000001");

        // when & then
        assertThatThrownBy(() -> point.charge(overMaxAmount))
                .isInstanceOf(ApiException.class)
                .extracting("apiErrorCode")
                .isEqualTo(ApiErrorCode.INVALID_REQUEST);
    }

    @Test
    void 포인트_충전시_현재잔액과_충전금액의_합이_최대한도를_초과하면_INVALID_REQUEST_예외가_발생한다() {
        // given
        Point point = Point.create(User.create("테스트유저"));
        point.charge(new BigDecimal("900000"));

        // when & then
        assertThatThrownBy(() -> point.charge(new BigDecimal("200000")))
                .isInstanceOf(ApiException.class)
                .extracting("apiErrorCode")
                .isEqualTo(ApiErrorCode.INVALID_REQUEST);
    }

    @Test
    void 포인트_충전시_정상적으로_충전되고_이력이_생성된다() {
        // given
        Point point = Point.create(User.create("테스트유저"));
        BigDecimal chargeAmount = new BigDecimal("10000");

        // when
        point.charge(chargeAmount);

        // then
        assertThat(point.getPoint()).isEqualTo(chargeAmount);
        assertThat(point.getHistories())
                .hasSize(1)
                .element(0)
                .extracting("amount")
                .isEqualTo(chargeAmount);
    }

    @Test
    void 포인트_사용시_잔액이_부족하면_INVALID_REQUEST_예외가_발생한다() {
        // given
        Point point = Point.create(User.create("테스트유저"));
        point.charge(new BigDecimal("10000"));
        BigDecimal useAmount = new BigDecimal("20000");

        // when & then
        assertThatThrownBy(() -> point.use(useAmount))
                .isInstanceOf(ApiException.class)
                .extracting("apiErrorCode")
                .isEqualTo(ApiErrorCode.INVALID_REQUEST);
    }

    @Test
    void 포인트_사용시_정상적으로_사용되고_이력이_생성된다() {
        // given
        Point point = Point.create(User.create("테스트유저"));
        point.charge(new BigDecimal("10000"));
        BigDecimal useAmount = new BigDecimal("5000");

        // when
        point.use(useAmount);

        // then
        assertThat(point.getPoint()).isEqualTo(new BigDecimal("5000"));
        assertThat(point.getHistories())
                .hasSize(2)
                .element(1)
                .extracting("amount")
                .isEqualTo(useAmount.negate());
    }
}