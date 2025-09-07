package kr.hhplus.be.server.interfaces.coupon.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import kr.hhplus.be.server.common.exception.ApiErrorCode;
import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.domain.coupon.domain.*;
import kr.hhplus.be.server.domain.user.domain.IUserRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.infra.outbox.CouponOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
    public void consume(List<String> messages) {
        List<CouponOutbox> unpublishedEvents = couponOutboxRepository.findUnpublishedEvents("Issue");
        List<CouponIssue> issuedCoupons = new ArrayList<>();
        List<CouponOutbox> updatedOutboxes = new ArrayList<>();
        log.info("배치 처리 시작: {} 개의 메시지 수신", messages.size());

        for (String message : messages) {
            CouponOutbox outbox = findMatchingOutbox(unpublishedEvents, message);
            if (outbox == null) {
                continue;
            }

            try {
                CouponEvent.Issue event = objectMapper.readValue(message, CouponEvent.Issue.class);
                Coupon coupon = couponRepository.findById(event.couponId()).get();
                User user = userRepository.findById(event.userId()).get();

                // 이미 발급된 쿠폰인지 확인
                if (couponRepository.isIssuedMember(event.couponId(), event.userId())) {
                    throw new ApiException(ApiErrorCode.CONFLICT);
                }

                CouponIssue issued = coupon.issue(user, event.createdAt());
                outbox.published();

                unpublishedEvents.remove(outbox);
                issuedCoupons.add(issued);
                updatedOutboxes.add(outbox);
            } catch (Exception e) {
                outbox.incrementFailedCount();
                updatedOutboxes.add(outbox);
            }
        }

        if (!issuedCoupons.isEmpty()) {
            couponRepository.saveAllCouponissues(issuedCoupons);
            for (CouponIssue couponIssue : issuedCoupons) { // 쿠폰 발급 이력을 캐시에 저장
                couponRepository.addIssuedCoupon(couponIssue.getCoupon().getId(), couponIssue.getUser().getId());
            }
        }
        if (!updatedOutboxes.isEmpty()) {
            couponOutboxRepository.saveAll(updatedOutboxes);
        }
    }

    private CouponOutbox findMatchingOutbox(List<CouponOutbox> unpublishedEvents, String message) {
        try {
            JsonNode messageJson = objectMapper.readTree(message);

            return unpublishedEvents.stream()
                    .filter(ob -> {
                        try {
                            return objectMapper.readTree(ob.getPayload()).equals(messageJson);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

}
