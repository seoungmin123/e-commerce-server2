package kr.hhplus.be.server.coupon.exception;

import kr.hhplus.be.server.common.response.ApiResponseCode;

public abstract class CouponException extends RuntimeException {
    private final ApiResponseCode errorCode;

    protected CouponException(ApiResponseCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApiResponseCode getErrorCode() {
        return errorCode;
    }
}