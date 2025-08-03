package kr.hhplus.be.server.product.exception;

import static kr.hhplus.be.server.common.response.ApiResponseCode.FAIL_INVALID_PRODUCT_QUANTITY_400;

public class InvalidStockQuantityException extends ProductException {
    public InvalidStockQuantityException(int quantity) {
        super(FAIL_INVALID_PRODUCT_QUANTITY_400, "재고 수량은 0보다 커야 합니다. 입력 값=" + quantity);
    }
}