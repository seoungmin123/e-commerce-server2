package kr.hhplus.be.server.interfaces.order.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.domain.order.domain.OrderEvent;
import kr.hhplus.be.server.infra.outbox.OrderOutboxJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {
    private final OrderOutboxJpaRepository orderOutboxJpaRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-completed", groupId = "order-group")
    @Transactional
    public void consumeOrderCompleted(String message) {
        try {
            OrderEvent.Completed event = objectMapper.readValue(message, OrderEvent.Completed.class);
            Long orderId = event.entityId();
            orderOutboxJpaRepository.findByOrderId(orderId).get().published();

            log.info("주문 완료 이벤트 수신: {}", message);
        } catch (JsonProcessingException e) {
            log.error("메시지 처리 실패: Json 변환 실패: {}", message, e);
        } catch (Exception e) {
            log.error("메시지 처리 실패: {}", message, e);
        }
    }
}
