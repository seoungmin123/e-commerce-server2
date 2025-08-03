package kr.hhplus.be.server.application.order;


import kr.hhplus.be.server.order.dto.OrderCreateCommand;
import kr.hhplus.be.server.product.dto.ValidatedProductInfo;
import kr.hhplus.be.server.user.domain.User;

import java.util.List;

public record OrderCreateCriteria(
        User user,
        List<OrderItemCriteria> products,
        Long couponIssueId
) {

    public List<OrderCreateCommand.OrderItemCommand> toOrderItemCommands() {
        return products.stream()
                .map(product -> new OrderCreateCommand.OrderItemCommand(
                        product.productId(),
                        null,
                        product.quantity()
                ))
                .toList();
    }

    public OrderCreateCommand toOrderCommand(List<ValidatedProductInfo> validateProducts) {
        return new OrderCreateCommand(
                this.user(),
                validateProducts.stream()
                        .map(product -> new OrderCreateCommand.OrderItemCommand(
                                product.product().getId(),
                                product.product(),
                                product.quantity()
                        ))
                        .toList(),
                couponIssueId
        );
    }


    public record OrderItemCriteria(
            Long productId,
            int quantity
    ) {
    }
}
