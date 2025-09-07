package kr.hhplus.be.server.domain.coupon.dto;

import kr.hhplus.be.server.domain.user.domain.User;

import java.math.BigDecimal;

public class CouponCommand {
    //쿠폰 발급 커맨드
    public record Issue(User user, Long couponId) {
    }

    //쿠폰 사용 커맨드
    public record Use(User user, Long couponIssueId, BigDecimal paymentAmount) {
        public static Use of(User user, Long couponIssueId, BigDecimal paymentAmount) {
            return new Use(user, couponIssueId, paymentAmount);
        }
    }
}
