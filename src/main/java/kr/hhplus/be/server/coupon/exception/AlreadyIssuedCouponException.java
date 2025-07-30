package kr.hhplus.be.server.coupon.exception;

import kr.hhplus.be.server.common.response.ApiResponseCode;

public class AlreadyIssuedCouponException extends CouponException {
    public AlreadyIssuedCouponException(Long userId, Long couponPolicyId) {
        super(ApiResponseCode.FAIL_CONFLICT_409,"이미 발급받은 쿠폰입니다. couponId : " + couponPolicyId +" userId: "+ userId);
    }
}