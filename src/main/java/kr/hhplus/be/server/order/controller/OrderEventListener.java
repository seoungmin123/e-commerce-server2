package kr.hhplus.be.server.order.controller;

import kr.hhplus.be.server.infra.external.order.ExternalOrderSender;
import kr.hhplus.be.server.infra.external.order.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Async
@Slf4j
@RequiredArgsConstructor
public class OrderEventListener {
    private final ExternalOrderSender externalOrderSender;

    @EventListener
    public void handlePaymentEvent(OrderEvent event) {
        log.info("Send to data platform - event: {}", event);
        externalOrderSender.send(event.toString());
    }
}