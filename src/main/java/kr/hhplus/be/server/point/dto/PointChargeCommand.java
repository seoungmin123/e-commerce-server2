package kr.hhplus.be.server.point.dto;

import kr.hhplus.be.server.user.domain.User;

import java.math.BigDecimal;

public record PointChargeCommand(
        User user,
        BigDecimal amount
) {
}
