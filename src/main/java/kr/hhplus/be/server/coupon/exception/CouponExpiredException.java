package kr.hhplus.be.server.coupon.exception;

import kr.hhplus.be.server.common.response.ApiResponseCode;

public class CouponExpiredException extends CouponException {
    public CouponExpiredException(Long couponId) {
        super(ApiResponseCode.FAIL_COUPON_EXPIRED_400, "만료된 쿠폰입니다: couponId=" + couponId);
    }
}