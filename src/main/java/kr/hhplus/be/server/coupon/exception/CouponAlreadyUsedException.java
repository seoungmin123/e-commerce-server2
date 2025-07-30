package kr.hhplus.be.server.coupon.exception;

import kr.hhplus.be.server.common.response.ApiResponseCode;

public class CouponAlreadyUsedException extends CouponException {
    public CouponAlreadyUsedException(Long couponId) {
        super(ApiResponseCode.FAIL_COUPON_ALREADY_USED_400, "이미 사용된 쿠폰입니다: couponId=" + couponId);
    }
}