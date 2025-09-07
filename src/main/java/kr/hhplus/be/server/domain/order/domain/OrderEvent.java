package kr.hhplus.be.server.domain.order.domain;

import kr.hhplus.be.server.domain.support.DomainEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderEvent {
    public record Completed(
            Long orderId,
            Long userId,
            Long paymentId,
            BigDecimal amount,
            LocalDateTime paidAt,
            LocalDateTime createdAt
    ) implements DomainEvent {
        public static Completed from(Order order) {
            return new Completed(
                    order.getId(),
                    order.getUser().getId(),
                    order.getId(),
                    order.getPaymentAmount(),
                    order.getCreatedAt(),
                    LocalDateTime.now()
            );
        }

        @Override
        public String eventType() {
            return this.getClass().getSimpleName();
        }

        @Override
        public Long entityId() {
            return orderId;
        }
    }
}