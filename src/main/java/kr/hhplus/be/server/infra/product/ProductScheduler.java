package kr.hhplus.be.server.infra.product;

import kr.hhplus.be.server.product.domain.PopularProductCacheManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductScheduler {
    private final PopularProductCacheManager popularProductCacheManager;

    @Async
    @Scheduled(cron = "0 */10 * * * *")
    @SchedulerLock(name = "refreshPopularProducts", lockAtLeastFor = "60s", lockAtMostFor = "60s")
    public void refreshPopularProducts() {
        log.info("refreshPopularProducts start");
        popularProductCacheManager.refreshPopularProducts();
        log.info("refreshPopularProducts end");
    }
}
