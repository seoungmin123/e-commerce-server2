package kr.hhplus.be.server.infra.payment;


import kr.hhplus.be.server.domain.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
}
