package kr.hhplus.be.server.payment.service;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.payment.domain.IPaymentRepository;
import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.payment.dto.PaymentCreateCommand;
import kr.hhplus.be.server.payment.dto.PaymentInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final IPaymentRepository paymentRepository;


    @Transactional
    public PaymentInfo pay(PaymentCreateCommand command) {
        Payment payment = Payment.create(command.orderId(), command.paymentAmount());
        payment = paymentRepository.save(payment);
        return PaymentInfo.from(payment);
    }
}
