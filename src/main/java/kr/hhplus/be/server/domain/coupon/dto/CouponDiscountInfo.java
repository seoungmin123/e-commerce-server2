package kr.hhplus.be.server.domain.coupon.dto;

import java.math.BigDecimal;

// 쿠폰 할인 정보
public record CouponDiscountInfo(
        Long couponIssueId,
        BigDecimal discountAmount
) {
}
