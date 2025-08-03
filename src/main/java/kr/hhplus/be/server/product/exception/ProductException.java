package kr.hhplus.be.server.product.exception;

import kr.hhplus.be.server.common.response.ApiResponseCode;

public abstract class ProductException extends RuntimeException {
    private final ApiResponseCode errorCode;

    protected ProductException(ApiResponseCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApiResponseCode getErrorCode() {
        return errorCode;
    }
}