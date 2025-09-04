package kr.hhplus.be.server.infra.outbox;


import kr.hhplus.be.server.domain.coupon.domain.CouponOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponOutboxJpaRepository extends JpaRepository<CouponOutbox, Long> {
    List<CouponOutbox> findAllByEventTypeAndStatus(String eventType, CouponOutbox.OutboxStatus outboxStatus);
}
