package kr.hhplus.be.server.product.exception;

import kr.hhplus.be.server.common.exception.NotFoundException;

public class ProductNotFoundException extends NotFoundException {
    public ProductNotFoundException(Long productId) {
        super("해당 상품을 찾을 수 없습니다. 제품 ID: " + productId);
    }

}