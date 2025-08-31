package kr.hhplus.be.server.interfaces.order.controller;

import kr.hhplus.be.server.infra.dataplatform.DataPlatformClient;
import kr.hhplus.be.server.domain.order.domain.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Async
@Slf4j
@RequiredArgsConstructor
public class OrderEventListener {
    private final DataPlatformClient dataPlatformClient;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentEvent(OrderCompletedEvent event) {
        log.info("Send to data platform - event: {}", event);
        dataPlatformClient.send(event.toString());
    }
}