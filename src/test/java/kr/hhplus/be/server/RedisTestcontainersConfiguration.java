package kr.hhplus.be.server;

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
class RedisTestcontainersConfiguration {

    public static final MySQLContainer<?> MYSQL_CONTAINER;
    //public static final RedisContainer REDIS_CONTAINER;
    public static final GenericContainer<?> REDIS_CONTAINER;
    private static final int REDIS_PORT = 6379;

    static {
        MYSQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"));
        // JPA 설정 추가
		System.setProperty("spring.jpa.hibernate.ddl-auto", "create");
		System.setProperty("spring.jpa.show-sql", "true");

            // Redis 컨테이너 설정
            REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:latest"))
                    .withExposedPorts(REDIS_PORT)
				.withCommand("redis-server", "--appendonly", "yes");
		REDIS_CONTAINER.start();

            // Redis 설정
            String redisHost = REDIS_CONTAINER.getHost();
            Integer redisMappedPort = REDIS_CONTAINER.getMappedPort(REDIS_PORT);
		System.setProperty("spring.data.redis.host", redisHost);
		System.setProperty("spring.data.redis.port", String.valueOf(redisMappedPort));

            // Redisson 설정
		System.setProperty("spring.redis.redisson.config",
                String.format("singleServerConfig:\n" +
                "  address: \"redis://%s:%d\"\n" +
                "  connectionMinimumIdleSize: 1\n" +
                "  connectionPoolSize: 10\n" +
                "  connectTimeout: 10000\n" +
                "  timeout: 3000", redisHost, redisMappedPort));

        }

    @PreDestroy
    public void preDestroy() {
        if (MYSQL_CONTAINER.isRunning()) {
            MYSQL_CONTAINER.stop();
        }
        if (REDIS_CONTAINER.isRunning()) {
            REDIS_CONTAINER.stop();
        }
    }
}