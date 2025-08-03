package kr.hhplus.be.server.product.controller;


import kr.hhplus.be.server.product.dto.ProductInfo;

import java.math.BigDecimal;

public record ProductResponse(
        Long productId,
        String name,
        BigDecimal price,
        int stock
) {
    public static ProductResponse from(ProductInfo product) {
        return new ProductResponse(product.productId(), product.name(), product.price(), product.stock());
    }
}
