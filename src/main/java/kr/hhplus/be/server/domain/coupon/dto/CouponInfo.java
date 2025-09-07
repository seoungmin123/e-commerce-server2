package kr.hhplus.be.server.domain.coupon.dto;

import kr.hhplus.be.server.domain.coupon.domain.CouponIssue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponInfo(
        Long couponId,
        Long couponIssueId,
        String name,
        String status,
        String discountType,
        BigDecimal discountAmount,
        LocalDateTime expiredAt,
        LocalDateTime usedAt,
        LocalDateTime createdAt
) {
    public static CouponInfo from(CouponIssue couponIssue) {
        return new CouponInfo(
                couponIssue.getCoupon().getId(),
                couponIssue.getId(),
                couponIssue.getCoupon().getName(),
                couponIssue.getStatus(),
                couponIssue.getCoupon().getDiscountType().getDescription(),
                couponIssue.getCoupon().getDiscountValue(),
                couponIssue.getExpiredAt(),
                couponIssue.getUsedAt() != null ? couponIssue.getUsedAt() : null,
                couponIssue.getCreatedAt()
        );
    }
}
