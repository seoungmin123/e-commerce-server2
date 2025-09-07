package kr.hhplus.be.server.infra;


import kr.hhplus.be.server.infra.test.KafkaPublisher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class KafkaIntegrationTest {
    @Autowired
    private KafkaPublisher kafkaPublisher;

    @Test
    void 카프카_퍼블리셔로_메시지_발행되면_카프카_컨슈머가_수신하고_로그에서_확인된다(CapturedOutput output) {
        // given
        String message = "카프카 메세지 발행된당";

        // when
        kafkaPublisher.publish("test-topic", message);

        // then : log.info 출력 확인
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Assertions.assertTrue(output.toString().contains("메시지 발행: " + message));
                    Assertions.assertTrue(output.toString().contains("메시지 수신: " + message));
                });
    }
}