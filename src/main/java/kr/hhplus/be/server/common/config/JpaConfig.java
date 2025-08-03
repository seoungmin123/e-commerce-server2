package kr.hhplus.be.server.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {
        "kr.hhplus.be.server.infra.coupon",
        "kr.hhplus.be.server.infra.order",
        "kr.hhplus.be.server.infra.payment",
        "kr.hhplus.be.server.infra.point",
        "kr.hhplus.be.server.infra.product",
        "kr.hhplus.be.server.infra.user"
})
public class JpaConfig {

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new JpaTransactionManager();
    }

}