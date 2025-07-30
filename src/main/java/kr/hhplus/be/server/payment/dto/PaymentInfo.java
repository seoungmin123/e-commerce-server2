package kr.hhplus.be.server.payment.dto;

import kr.hhplus.be.server.payment.domain.Payment;

import java.math.BigDecimal;

public record PaymentInfo(
        Long paymentId,
        Long orderId,
        BigDecimal paymentAmount
) {
    public static PaymentInfo from(Payment payment) {
        return new PaymentInfo(payment.getId(), payment.getOrderId(), payment.getPaymentAmount());
    }
}
