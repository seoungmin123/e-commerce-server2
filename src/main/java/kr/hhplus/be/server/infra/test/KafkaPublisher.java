package kr.hhplus.be.server.infra.test;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publish(String topic, String message) {
        kafkaTemplate.send(topic, message);
        log.info("메시지 발행: {} ", message);
    }

}