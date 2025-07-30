package kr.hhplus.be.server.order.controller;


import kr.hhplus.be.server.order.dto.OrderInfo;
import kr.hhplus.be.server.order.facade.OrderResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        Long orderId,
        String status,
        BigDecimal totalAmount,
        int totalQuantity,
        LocalDateTime createdAt
) {
    public static OrderResponse from(OrderResult orderResult) {
        return new OrderResponse(
                orderResult.orderId(),
                orderResult.status(),
                orderResult.totalAmount(),
                orderResult.totalQuantity(),
                orderResult.createdAt()
        );
    }

    public static OrderResponse from(OrderInfo orderInfo) {
        return new OrderResponse(
                orderInfo.orderId(),
                orderInfo.status(),
                orderInfo.totalAmount(),
                orderInfo.totalQuantity(),
                orderInfo.createdAt()
        );
    }
}
