package kr.hhplus.be.server.interfaces.order.controller;

import kr.hhplus.be.server.domain.order.domain.OrderEvent;
import kr.hhplus.be.server.infra.order.OrderEventPublisher;
import kr.hhplus.be.server.infra.outbox.OrderOutBoxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventListener {
    private final OrderEventPublisher eventPublisher;
    private final OrderOutBoxRepository orderOutboxRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveToOutbox(OrderEvent.Completed event) {
        orderOutboxRepository.save(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCompletedEvent(OrderEvent.Completed event) {
        eventPublisher.publish("order-completed", event);
    }
}