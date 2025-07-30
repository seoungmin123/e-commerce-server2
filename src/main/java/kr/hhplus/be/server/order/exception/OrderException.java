package kr.hhplus.be.server.order.exception;

import kr.hhplus.be.server.common.response.ApiResponseCode;

public abstract class OrderException extends RuntimeException {
    private final ApiResponseCode errorCode;

    protected OrderException(ApiResponseCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApiResponseCode getErrorCode() {
        return errorCode;
    }
}