package kr.hhplus.be.server.order.dto;

import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.dto.ValidatedProductInfo;
import kr.hhplus.be.server.user.domain.User;

import java.math.BigDecimal;
import java.util.List;


public class OrderCommand {
    public record Order(User user, List<Item> products, Long couponIssueId) {
        public Order with(List<ValidatedProductInfo> validateProducts) {
            return new Order(
                    this.user(),
                    validateProducts.stream()
                            .map(validatedProductInfo -> new Item(
                                    validatedProductInfo.product().getId(),
                                    validatedProductInfo.product(),
                                    validatedProductInfo.quantity()
                            ))
                            .toList(),
                    this.couponIssueId()
            );
        }
    }

    public record Item(Long productId, Product product, int quantity) {
    }

    public record Confirm(Long orderId) {
        public static Confirm from(Long orderId) {
            return new Confirm(orderId);
        }
    }

    public record ApplyCoupon(Long orderId, Long couponIssueId, BigDecimal discountAmount) {
        public static ApplyCoupon of(Long orderId, Long couponIssueId, BigDecimal discountAmount) {
            return new ApplyCoupon(orderId, couponIssueId, discountAmount);
        }
    }
}

