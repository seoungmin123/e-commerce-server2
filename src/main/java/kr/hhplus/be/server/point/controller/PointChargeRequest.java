package kr.hhplus.be.server.point.controller;


import kr.hhplus.be.server.point.dto.PointCommand;
import kr.hhplus.be.server.user.domain.User;

import java.math.BigDecimal;

public record PointChargeRequest(
        User user,
        BigDecimal amount
) {
    public PointCommand.Charge toCommand() {

        return new PointCommand.Charge(
                this.user, this.amount());
    }
}
