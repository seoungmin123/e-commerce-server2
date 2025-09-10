package kr.hhplus.be.server.interfaces.coupon.controller;


import kr.hhplus.be.server.domain.coupon.domain.CouponEvent;
import kr.hhplus.be.server.domain.coupon.domain.CouponOutbox;
import kr.hhplus.be.server.infra.coupon.CouponEventPublisher;
import kr.hhplus.be.server.infra.outbox.CouponOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class CouponEventListener {
    private final CouponEventPublisher eventPublisher;
    private final CouponOutboxRepository couponOutboxRepository;


    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveToOutbox(CouponEvent.Issue event) {
        CouponOutbox outbox = couponOutboxRepository.save(event);
        log.info("쿠폰 이벤트 Outbox 저장: outboxId={}, couponId={}, userId={}",
                outbox.getId(), event.couponId(), event.userId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCompletedEvent(CouponEvent.Issue event) {
        log.info("쿠폰 이벤트 발행 시작: couponId={}, userId={}", event.couponId(), event.userId());
        eventPublisher.publishV1("coupon-issue", event);
        log.info("쿠폰 이벤트 발행 완료: couponId={}, userId={}", event.couponId(), event.userId());
    }
}