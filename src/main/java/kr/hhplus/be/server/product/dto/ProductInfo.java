package kr.hhplus.be.server.product.dto;

import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.ProductStock;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductInfo(
        Long productId,
        String name,
        BigDecimal price,
        int stock
) {
    public static ProductInfo of(Product product, ProductStock stock){
        return ProductInfo.builder()
                .productId(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(stock.getQuantity())
                .build();
    }

}