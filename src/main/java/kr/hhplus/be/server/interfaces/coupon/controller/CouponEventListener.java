package kr.hhplus.be.server.interfaces.coupon.controller;


import kr.hhplus.be.server.domain.coupon.domain.CouponEvent;
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
        couponOutboxRepository.save(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCompletedEvent(CouponEvent.Issue event) {
        eventPublisher.publish("coupon-issue", event);
    }
}
