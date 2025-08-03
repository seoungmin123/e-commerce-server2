package kr.hhplus.be.server.payment.controller;


import kr.hhplus.be.server.payment.dto.PaymentCreateCommand;
import kr.hhplus.be.server.user.domain.User;

import java.math.BigDecimal;

public record PaymentCreateRequest(
        User user,
        Long orderId,
        BigDecimal paymentAmount
) {
    public PaymentCreateCommand toCommand() {
        return new PaymentCreateCommand(user, orderId, paymentAmount);
    }
}
