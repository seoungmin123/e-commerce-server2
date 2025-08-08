package kr.hhplus.be.server.point.dto;

import kr.hhplus.be.server.user.domain.User;

import java.math.BigDecimal;

public class PointCommand {
    public record Charge(User user, BigDecimal amount) {
    }

    public record Use(User user, BigDecimal amount) {
        public static Use of(User user, BigDecimal bigDecimal) {
            return new Use(user, bigDecimal);
        }
    }
}

