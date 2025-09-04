package kr.hhplus.be.server.domain.coupon.domain;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.support.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "coupon_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponOutbox extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "event_type")
    private String eventType;

    @Column(columnDefinition = "json")
    private String payload;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    @Column(name = "failed_count")
    private int failedCount = 0;

    @Column(name = "last_failed_time")
    private LocalDateTime lastFailedTime;

    @Builder
    public CouponOutbox(Long couponId, String eventType, String payload) {
        this.couponId = couponId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.INIT;
        this.failedCount = 0;
    }

    public void published() {
        this.status = OutboxStatus.PUBLISHED;
    }

    public void incrementFailedCount() {
        this.failedCount++;
        this.lastFailedTime = LocalDateTime.now();
    }

    public enum OutboxStatus {
        INIT, PUBLISHED
    }
}