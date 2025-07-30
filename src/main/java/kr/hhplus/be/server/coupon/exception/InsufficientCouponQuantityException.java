package kr.hhplus.be.server.coupon.exception;

import kr.hhplus.be.server.common.response.ApiResponseCode;

public class InsufficientCouponQuantityException extends CouponPolicyException {
    public InsufficientCouponQuantityException() {
        super(ApiResponseCode.FAIL_CONFLICT_409, "쿠폰 발급 수량이 모두 소진되었습니다.");
    }
}