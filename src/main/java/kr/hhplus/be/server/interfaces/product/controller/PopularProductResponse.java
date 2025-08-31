package kr.hhplus.be.server.interfaces.product.controller;

import kr.hhplus.be.server.domain.product.dto.PopularProductInfo;
import java.math.BigDecimal;

public record PopularProductResponse(
        Long productId,
        String name,
        BigDecimal price,
        int totalQuantity
) {
    public static PopularProductResponse from(PopularProductInfo popularProductInfo) {
        return new PopularProductResponse(
                popularProductInfo.productId(),
                popularProductInfo.name(),
                popularProductInfo.price(),
                popularProductInfo.totalQuantity()
        );
    }
}

