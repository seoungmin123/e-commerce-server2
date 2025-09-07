package kr.hhplus.be.server.infra.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.domain.coupon.domain.CouponOutbox;
import kr.hhplus.be.server.domain.support.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CouponOutboxRepository {
    private final CouponOutboxJpaRepository couponOutboxJpaRepository;
    private final ObjectMapper objectMapper;

    public CouponOutbox save(DomainEvent event) {
        try {
            CouponOutbox outbox = CouponOutbox.builder()
                    .couponId(event.entityId())
                    .eventType(event.eventType())
                    .payload(objectMapper.writeValueAsString(event))
                    .build();

            return couponOutboxJpaRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json 변환 실패: " + event.eventType(), e);
        }
    }

    public List<CouponOutbox> findUnpublishedEvents(String eventType) {
        return couponOutboxJpaRepository.findAllByEventTypeAndStatus(eventType, CouponOutbox.OutboxStatus.INIT);
    }

    public void saveAll(List<CouponOutbox> publishedOutboxes) {
        couponOutboxJpaRepository.saveAll(publishedOutboxes);
    }

    public List<CouponOutbox> findAll() {
        return couponOutboxJpaRepository.findAll();
    }
}
