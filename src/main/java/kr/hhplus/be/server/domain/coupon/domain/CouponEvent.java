package kr.hhplus.be.server.domain.coupon.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import kr.hhplus.be.server.domain.support.DomainEvent;

import java.time.LocalDateTime;

public class CouponEvent {
    @JsonPropertyOrder({"userId", "couponId", "createdAt"})
    public record Issue(
            Long couponId,
            Long userId,
            LocalDateTime createdAt
    ) implements DomainEvent {
        public static Issue of(Long couponId, Long userId) {
            return new Issue(
                    couponId,
                    userId,
                    LocalDateTime.now()
            );
        }

        @Override
        public String eventType() {
            return this.getClass().getSimpleName();
        }

        @Override
        public Long entityId() {
            return couponId;
        }
    }
}