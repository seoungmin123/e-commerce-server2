package kr.hhplus.be.server.order.dto;

import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.user.domain.User;

import java.util.List;

public record OrderCreateCommand(
        User user,
        List<OrderItemCommand> products,
        Long couponIssueId
        ) {

    public record OrderItemCommand(
            Long productId,
            Product product,
            int quantity
    ) {
    }

}
