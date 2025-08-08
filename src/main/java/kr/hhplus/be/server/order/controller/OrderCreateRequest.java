package kr.hhplus.be.server.order.controller;

import kr.hhplus.be.server.application.order.OrderCriteria;
import kr.hhplus.be.server.user.domain.User;

import java.util.List;

public record OrderCreateRequest(
        User user,
        List<OrderProductRequest> products,
        Long couponIssueId
) {
    public OrderCriteria.Order toCriteria() {
        return new OrderCriteria.Order(
                this.user(),
                this.products().stream()
                        .map(orderProductRequest -> orderProductRequest.toCriteria())
                        .toList(),
                this.couponIssueId()
        );
    }

    public record OrderProductRequest (
            Long productId,
            int quantity
    ){
        public OrderCriteria.Item toCriteria() {
            return new OrderCriteria.Item(
                    this.productId(),
                    this.quantity()
            );
        }
    }
}

