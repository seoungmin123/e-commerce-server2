package kr.hhplus.be.server.payment.dto;

import kr.hhplus.be.server.user.domain.User;

import java.math.BigDecimal;

public record PaymentCreateCommand(
        User user,
        Long orderId,
        BigDecimal paymentAmount
) {
    public static PaymentCreateCommand from(User user, Long orderId, BigDecimal paymentAmount) {
        return new PaymentCreateCommand(user, orderId, paymentAmount);
    }
}
