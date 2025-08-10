package kr.hhplus.be.server.payment.controller;


import kr.hhplus.be.server.payment.dto.PaymentCommand;
import kr.hhplus.be.server.user.domain.User;

import java.math.BigDecimal;

public record PaymentCreateRequest(
        User user,
        Long orderId,
        BigDecimal paymentAmount
) {
    public PaymentCommand.Pay toCommand() {
        return new PaymentCommand.Pay(user, orderId, paymentAmount);
    }
}
