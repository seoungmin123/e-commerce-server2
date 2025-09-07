package kr.hhplus.be.server.domain.order.domain;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.support.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "order_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderOutbox extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "event_type")
    private String eventType;

    @Column(columnDefinition = "json")
    private String payload;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    @Column(name = "retry_count")
    private int retryCount;

    @Column(name = "last_retry_time")
    private LocalDateTime lastRetryTime;

    public enum OutboxStatus {
        INIT, PUBLISHED
    }

    @Builder
    public OrderOutbox(Long orderId, String eventType, String payload) {
        this.orderId = orderId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.INIT;
        this.retryCount = 0;
    }

    public void published() {
        this.status = OutboxStatus.PUBLISHED;
    }

    public void incrementRetryCount() {
        this.retryCount++;
        this.lastRetryTime = LocalDateTime.now();
    }
}