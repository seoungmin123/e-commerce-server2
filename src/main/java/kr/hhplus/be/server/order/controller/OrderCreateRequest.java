package kr.hhplus.be.server.order.controller;

import kr.hhplus.be.server.order.facade.OrderCreateCriteria;
import kr.hhplus.be.server.user.domain.User;

import java.util.List;

public record OrderCreateRequest(
        User user,
        List<OrderProductRequest> products,
        Long couponIssueId
) {
    public OrderCreateCriteria toCriteria() {
        return new OrderCreateCriteria(
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
        public OrderCreateCriteria.OrderItemCriteria toCriteria() {
            return new OrderCreateCriteria.OrderItemCriteria(
                    this.productId(),
                    this.quantity()
            );
        }
    }
}

