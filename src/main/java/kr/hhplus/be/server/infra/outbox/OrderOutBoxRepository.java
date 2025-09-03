package kr.hhplus.be.server.infra.outbox;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.domain.order.domain.OrderOutbox;
import kr.hhplus.be.server.domain.support.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderOutBoxRepository {
    private final OrderOutboxJpaRepository orderOutBoxJpaRepository;
    private final ObjectMapper objectMapper;

    public OrderOutbox save(DomainEvent event) {
        try {
            OrderOutbox outbox = OrderOutbox.builder()
                    .orderId(event.entityId())
                    .eventType(event.eventType())
                    .payload(objectMapper.writeValueAsString(event))
                    .build();

            return orderOutBoxJpaRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json 변환 실패: " + event.eventType(), e);
        }
    }

    public Optional<OrderOutbox> findByOrderId(Long orderId) {
        return orderOutBoxJpaRepository.findByOrderId(orderId);
    }

    public List<OrderOutbox> findUnpublishedEvents(String eventType) {
        return orderOutBoxJpaRepository.findAllByEventTypeAndStatus(eventType, OrderOutbox.OutboxStatus.INIT);
    }

    public void saveAll(List<OrderOutbox> outboxList) {
        orderOutBoxJpaRepository.saveAll(outboxList);
    }
}
