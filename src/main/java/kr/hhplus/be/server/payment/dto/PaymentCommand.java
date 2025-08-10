package kr.hhplus.be.server.payment.dto;

import kr.hhplus.be.server.user.domain.User;

import java.math.BigDecimal;

public class PaymentCommand {
    public record Pay(User user, Long orderId, BigDecimal paymentAmount) {
        public static Pay of(User user, Long orderId, BigDecimal paymentAmount) {
            return new Pay(user, orderId, paymentAmount);
        }
    }
}