package kr.hhplus.be.server.domain.service;


import kr.hhplus.be.server.domain.coupon.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.domain.CouponOutbox;
import kr.hhplus.be.server.domain.coupon.domain.ICouponRepository;
import kr.hhplus.be.server.domain.coupon.dto.CouponCommand;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.user.domain.IUserRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.infra.outbox.CouponOutboxRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Sql(scripts = {"/cleanup.sql", "/test-data.sql"})
public class CouponEventIntegrationTest {
    @Autowired
    private CouponService couponService;

    @Autowired
    private ICouponRepository couponRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private CouponOutboxRepository couponOutboxRepository;

    @Test
    void 쿠폰_발급_요청시_대기큐와_outbox에_들어가고_Consumer에서_정상적으로_발급된다() throws Exception {
        // given
        Long couponId = 1L;
        Long userId = 5L;

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));
        Coupon coupon = couponRepository.findById(couponId).orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));

        CouponCommand.Issue command = new CouponCommand.Issue(user, couponId);

        // when
        boolean enqueued = couponService.enqueue(command);

        // then
        assertTrue(enqueued, "쿠폰 발급 요청이 큐에 들어간다");

        // Outbox에 이벤트가 저장되었는지 확인
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<CouponOutbox> events = couponOutboxRepository.findAll();
                    assertFalse(events.isEmpty());

                    Optional<CouponOutbox> event = events.stream()
                            .filter(e -> e.getCouponId().equals(couponId))
                            .findFirst();

                    assertTrue(event.isPresent(), "이벤트가 Outbox에 저장된다");
                });

        // 실제 쿠폰이 발급되었는지 확인 (Consumer 처리)
        await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    boolean isIssued = couponRepository.isIssuedMember(couponId, userId);
                    assertTrue(isIssued, "쿠폰이 사용자에게 발급되었다");

                    // 쿠폰 발급 수량 확인
                    Coupon updatedCoupon = couponRepository.findById(couponId).orElseThrow();
                    assertEquals(1, updatedCoupon.getIssuedQuantity() - coupon.getIssuedQuantity()
                    );
                });
    }

}
