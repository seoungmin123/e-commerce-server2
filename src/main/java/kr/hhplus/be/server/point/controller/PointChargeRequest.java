package kr.hhplus.be.server.point.controller;


import kr.hhplus.be.server.point.dto.PointChargeCommand;
import kr.hhplus.be.server.user.domain.User;

import java.math.BigDecimal;

public record PointChargeRequest(
        User user,
        BigDecimal amount
) {
    public PointChargeCommand toCommand() {

        return new PointChargeCommand(
                this.user, this.amount());
    }
}
