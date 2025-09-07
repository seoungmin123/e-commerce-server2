package kr.hhplus.be.server.domain.product.dto;

import kr.hhplus.be.server.domain.product.domain.Product;

public record ValidatedProductInfo(
        Product product,
        int quantity
) {
    public static ValidatedProductInfo of(Product product, int quantity) {
        return new ValidatedProductInfo(product, quantity);
    }
}
