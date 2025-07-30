package kr.hhplus.be.server.product.dto;

import java.math.BigDecimal;

public record PopularProductQuery(
    Long productId,
    String name,
    BigDecimal price,
    int totalQuantity
) {

    public PopularProductInfo toInfo(long rank) {
        return new PopularProductInfo(rank, productId, name, price, totalQuantity);
    }
}