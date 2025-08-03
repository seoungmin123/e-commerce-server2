package kr.hhplus.be.server.coupon.exception;

import kr.hhplus.be.server.common.response.ApiResponseCode;

public abstract class CouponPolicyException extends RuntimeException {
    private final ApiResponseCode errorCode;

    protected CouponPolicyException(ApiResponseCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApiResponseCode getErrorCode() {
        return errorCode;
    }
}