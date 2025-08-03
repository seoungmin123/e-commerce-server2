package kr.hhplus.be.server.order.controller;

import kr.hhplus.be.server.application.order.OrderCriteria;
import kr.hhplus.be.server.user.domain.User;

import java.util.List;

public record OrderCreateRequest(
        User user,
        List<OrderProductRequest> products,
        Long couponIssueId
) {
    public OrderCriteria.Create toCriteria() {
        return new OrderCriteria.Create(
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

