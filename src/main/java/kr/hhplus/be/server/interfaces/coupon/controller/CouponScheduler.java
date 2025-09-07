package kr.hhplus.be.server.interfaces.coupon.controller;


import kr.hhplus.be.server.domain.coupon.domain.CouponIssueProcessor;
import kr.hhplus.be.server.domain.coupon.domain.ICouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class CouponScheduler {
    private final ICouponRepository couponRepository;
    private final CouponIssueProcessor couponIssueProcessor;

    @Scheduled(cron = "0 */1 * * * *")
    @SchedulerLock(name = "issueCoupon", lockAtLeastFor = "59s", lockAtMostFor = "59s")
    public void issueCoupon() {
        log.info("issueCoupon start");
        // 각 쿠폰에 대해 발급 처리
        couponRepository.findIssuableCoupons(LocalDateTime.now().minusMinutes(1)).forEach(couponIssueProcessor::processCouponIssuance);
        log.info("issueCoupon end");
    }


}
