package kr.hhplus.be.server.interfaces.order.controller;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.order.domain.OrderOutbox;
import kr.hhplus.be.server.infra.outbox.OrderOutBoxRepository;
import kr.hhplus.be.server.infra.test.KafkaPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderScheduler {
    private final OrderOutBoxRepository orderOutBoxRepository;
    private final KafkaPublisher kafkaPublisher;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void republishUnpublishedEvents() {
        List<OrderOutbox> failedEvents = new ArrayList<>();
        try {
            List<OrderOutbox> unpublishedEvents = orderOutBoxRepository.findUnpublishedEvents("OrderCompletedEvent");

            for (OrderOutbox event : unpublishedEvents) {
                try {
                    kafkaPublisher.publish("order-completed", event.getPayload());
                } catch (Exception e) {
                    event.incrementRetryCount();
                    failedEvents.add(event);
                    log.error("이벤트 재발행 실패 - orderId: {}, eventType: {}", event.getOrderId(), event.getEventType(), e);
                }
            }
        } catch (Exception e) {
            log.error("이벤트 재발행 프로세스 실패", e);
        } finally {
            if (!failedEvents.isEmpty()) {
                orderOutBoxRepository.saveAll(failedEvents);
            }
        }
    }


}
