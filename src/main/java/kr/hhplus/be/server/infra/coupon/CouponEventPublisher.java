package kr.hhplus.be.server.infra.coupon;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.domain.support.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(String topic, DomainEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            log.info("메시지 발행: {}", message);
            // key 지정하여 같은 couponId로 메시지를 보내면 같은 파티션으로 메시지가 전달
            kafkaTemplate.send(topic, String.valueOf(event.entityId()), message);
        } catch (Exception e) {
            log.error("메시지 발행 실패: {}", event, e);
            throw new RuntimeException("메시지 발행 실패", e);
        }
    }
}