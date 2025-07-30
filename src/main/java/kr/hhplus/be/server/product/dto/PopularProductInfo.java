package kr.hhplus.be.server.product.dto;

import java.math.BigDecimal;

public record PopularProductInfo(
        Long rank,
        Long productId,
        String name,
        BigDecimal price,
        int totalQuantity
) {
}
