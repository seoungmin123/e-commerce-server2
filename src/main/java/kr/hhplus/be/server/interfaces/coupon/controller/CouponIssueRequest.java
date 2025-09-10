package kr.hhplus.be.server.interfaces.coupon.controller;

import kr.hhplus.be.server.domain.coupon.dto.CouponCommand;

import java.time.LocalDateTime;

public record CouponIssueRequest(
        Long userId
) {
    public CouponCommand.IssueCouponForKafka toCommand(Long couponId) {
        return new CouponCommand.IssueCouponForKafka(userId, couponId, LocalDateTime.now());
    }
}
