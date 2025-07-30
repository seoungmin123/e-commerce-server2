package kr.hhplus.be.server.product.exception;

import kr.hhplus.be.server.common.response.ApiResponseCode;

public class InsufficientStockException extends ProductException {
    public InsufficientStockException(Long productId, int stock, int requested) {
        super(ApiResponseCode.FAIL_PRODUCT_STOCK_SHORTAGE_400,
                "재고 부족: productId=" + productId + ", 현재 재고=" + stock + ", 요청 수량=" + requested);

    }
}