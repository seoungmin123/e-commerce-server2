package kr.hhplus.be.server.product.dto;

import kr.hhplus.be.server.product.domain.Product;

import java.math.BigDecimal;

public record ProductInfo(
        Long productId,
        String name,
        BigDecimal price,
        int stock
) {
    public static ProductInfo from(Product product) {
        return new ProductInfo(product.getId(), product.getName(), product.getPrice(), product.getProductStock().getQuantity());
    }
}
