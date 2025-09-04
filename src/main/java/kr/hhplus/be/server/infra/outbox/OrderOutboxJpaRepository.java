package kr.hhplus.be.server.infra.outbox;


import kr.hhplus.be.server.domain.order.domain.OrderOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderOutboxJpaRepository  extends JpaRepository<OrderOutbox, Long> {

    Optional<OrderOutbox> findByOrderId(Long orderId);

    List<OrderOutbox> findAllByEventTypeAndStatus(String eventType, OrderOutbox.OutboxStatus status);
}
