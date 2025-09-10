package kr.hhplus.be.server.interfaces.coupon.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.coupon.domain.CouponOutbox;
import kr.hhplus.be.server.domain.coupon.domain.ICouponRepository;
import kr.hhplus.be.server.domain.coupon.dto.CouponCommand;
import kr.hhplus.be.server.domain.user.domain.IUserRepository;
import kr.hhplus.be.server.infra.outbox.CouponOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CouponEventConsumer {
    private final ObjectMapper objectMapper;
    private final ICouponRepository couponRepository;
    private final IUserRepository userRepository;
    private final CouponOutboxRepository couponOutboxRepository;


    @KafkaListener(
            topics = "coupon-issue",
            groupId = "coupon-group",
            containerFactory = "batchContainerFactory"
    )
    @Transactional
    public void consume(CouponCommand.IssueCouponForKafka event) {
        // 1. 쿠폰 수량 검증

        // 2. 쿠폰 중복 발급 검증

        // 3. 쿠폰 발급

//        ---임시
        System.out.println("event.userId() = " + event.userId());
        System.out.println("event.couponId() = " + event.couponId());
    }


    private CouponOutbox findMatchingOutbox(List<CouponOutbox> unpublishedEvents, String message) {
        try {
            JsonNode messageJson = objectMapper.readTree(message);
            Long couponId = messageJson.get("couponId").asLong();
            Long userId = messageJson.get("userId").asLong();

            log.info("찾는 메시지: couponId={}, userId={}", couponId, userId);

            return unpublishedEvents.stream()
                    .filter(ob -> {
                        try {
                            JsonNode outboxJson = objectMapper.readTree(ob.getPayload());
                            Long outboxCouponId = outboxJson.get("couponId").asLong();
                            Long outboxUserId = outboxJson.get("userId").asLong();

                            log.info("Outbox 비교: outboxId={}, couponId={}, userId={}",
                                    ob.getId(), outboxCouponId, outboxUserId);

                            return outboxCouponId.equals(couponId) && outboxUserId.equals(userId);
                        } catch (Exception e) {
                            log.error("Outbox 페이로드 파싱 실패: {}", e.getMessage());
                            return false;
                        }
                    })
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.error("메시지 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    }
