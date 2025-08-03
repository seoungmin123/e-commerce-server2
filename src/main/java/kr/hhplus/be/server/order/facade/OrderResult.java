package kr.hhplus.be.server.order.facade;


import kr.hhplus.be.server.order.dto.OrderInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResult(
        Long orderId,
        String status,
        BigDecimal totalAmount,
        Integer totalQuantity,
        LocalDateTime createdAt
) {
    public static OrderResult from(OrderInfo orderInfo) {
        return new OrderResult(
                orderInfo.orderId(),
                orderInfo.status(),
                orderInfo.totalAmount(),
                orderInfo.totalQuantity(),
                orderInfo.createdAt()
        );
    }
}
