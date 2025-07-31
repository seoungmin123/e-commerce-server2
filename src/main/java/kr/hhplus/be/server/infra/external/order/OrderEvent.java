package kr.hhplus.be.server.infra.external.order;


import kr.hhplus.be.server.order.domain.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderEvent(
        Long orderId,
        Long userId,
        Long paymentId,
        BigDecimal amount,
        LocalDateTime paidAt
) {
    public static OrderEvent from(Order order) {
        return new OrderEvent(
                order.getId(),
                order.getUser().getId(),
                order.getId(),
                order.getPaymentAmount(),
                order.getCreatedAt()
        );
    }
}
