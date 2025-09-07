package kr.hhplus.be.server.domain.service;

import kr.hhplus.be.server.domain.coupon.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.domain.CouponIssueProcessor;
import kr.hhplus.be.server.domain.coupon.domain.ICouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Sql(scripts = {"/cleanup.sql", "/test-data.sql"})
class CouponIssueProcessorIntegrationTest {
    @Autowired
    private CouponIssueProcessor couponIssueProcessor;
    @Autowired
    private ICouponRepository couponRepository;
    @Autowired
    private RedisTemplate<String, Long> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.flushDb();
            return null;
        });
    }
    @Test
    void 쿠폰발급시_발급가능_수량이_충분하면_모든요청을처리한다() {
        // given
        Coupon coupon = couponRepository.findById(5L).get();

        // 쿠폰 발급 요청 6건
        for (long userId = 1; userId <= 6; userId++) {
            couponRepository.addRequest(coupon.getId(), userId);
        }

        // when
        couponIssueProcessor.processCouponIssuance(coupon);

        // then
        assertThat(couponRepository.getIssuedCount(coupon.getId())).isEqualTo(6);
        assertThat(couponRepository.getRequestCount(coupon.getId())).isEqualTo(0);
    }


    @Test
    void 발급기한이_지난_쿠폰발급시_실패목록에추가된다() {
        // given
        Coupon expiredCoupon = couponRepository.findById(6L).get();

        for (long userId = 1; userId <= 3; userId++) {
            couponRepository.addRequest(expiredCoupon.getId(), userId);
        }

        // when
        couponIssueProcessor.processCouponIssuance(expiredCoupon);

        // then
        assertThat(couponRepository.getIssuedCount(expiredCoupon.getId())).isEqualTo(0);
        assertThat(couponRepository.getFailedCount(expiredCoupon.getId())).isEqualTo(3);
    }

    @Test
    void 쿠폰발급시_잔여수량보다_요청이_많으면_잔여수량만큼만_처리한다() {
        // given
        Coupon limitedCoupon = couponRepository.findById(7L).get();

        // 3명이 요청했지만 2장만 발급 가능
        for (long userId = 1; userId <= 3; userId++) {
            couponRepository.addRequest(limitedCoupon.getId(), userId);
        }

        // when
        couponIssueProcessor.processCouponIssuance(limitedCoupon);

        // then
        assertThat(couponRepository.getIssuedCount(limitedCoupon.getId())).isEqualTo(2);
        assertThat(couponRepository.getRequestCount(limitedCoupon.getId())).isEqualTo(1);
    }


}