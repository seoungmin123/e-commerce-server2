package kr.hhplus.be.server.interfaces.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaConsumer {
    @KafkaListener(topics = "test-topic", groupId = "test-group")
    public void consume(String message) {
        log.info("메시지 수신: {}", message);
    }
}