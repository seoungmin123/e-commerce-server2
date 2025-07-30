package kr.hhplus.be.server.order.dto;

import kr.hhplus.be.server.order.domain.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderInfo(
        Long orderId,
        String status,
        BigDecimal totalAmount,
        BigDecimal paymentAmount,
        Integer totalQuantity,
        LocalDateTime createdAt
) {
    public static OrderInfo from(Order order) {
        return new OrderInfo(
                order.getId(),
                order.getStatus().getDescription(),
                order.getTotalAmount(),
                order.getPaymentAmount(),
                order.getTotalQuantity(),
                order.getCreatedAt()
        );
    }
}
