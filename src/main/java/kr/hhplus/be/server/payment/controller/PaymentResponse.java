package kr.hhplus.be.server.payment.controller;


import kr.hhplus.be.server.payment.dto.PaymentInfo;

import java.math.BigDecimal;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        BigDecimal paymentAmount
) {
    public static PaymentResponse from(PaymentInfo paymentInfo) {
        return new PaymentResponse(paymentInfo.paymentId(), paymentInfo.orderId(), paymentInfo.paymentAmount());
    }
}
