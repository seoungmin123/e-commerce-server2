package kr.hhplus.be.server;

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
class KafkaContainersConfiguration {

    public static final KafkaContainer KAFKA_CONTAINER;

    static {
        // Kafka 컨테이너 설정
        KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.3"))
                .withReuse(false);
        KAFKA_CONTAINER.start();

        // Kafka 설정
        System.setProperty("spring.kafka.bootstrap-servers", KAFKA_CONTAINER.getBootstrapServers());
        System.setProperty("spring.kafka.consumer.auto-offset-reset", "earliest");
        System.setProperty("spring.kafka.consumer.group-id", "test-group");
    }

    @PreDestroy
    public void preDestroy() {

        if (KAFKA_CONTAINER.isRunning()) {
            KAFKA_CONTAINER.stop();
        }
    }
}