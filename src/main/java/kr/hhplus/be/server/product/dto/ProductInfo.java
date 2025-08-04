package kr.hhplus.be.server.product.dto;

import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.ProductStock;

import java.math.BigDecimal;

public record ProductInfo(
        Long productId,
        String name,
        BigDecimal price,
        int stock
) {
    public static ProductInfo of(Product product, ProductStock stock) {
        return new ProductInfo(product.getId(), product.getName(), product.getPrice(), stock.getQuantity());
    }
}
