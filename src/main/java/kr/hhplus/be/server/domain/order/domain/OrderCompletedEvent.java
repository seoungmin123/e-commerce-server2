package kr.hhplus.be.server.domain.order.domain;


import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderCompletedEvent(
        Long orderId,
        Long userId,
        Long paymentId,
        BigDecimal amount,
        LocalDateTime paidAt
) {
    public static OrderCompletedEvent from(Order order) {
        return new OrderCompletedEvent(
                order.getId(),
                order.getUser().getId(),
                order.getId(),
                order.getPaymentAmount(),
                order.getCreatedAt()
        );
    }
}
