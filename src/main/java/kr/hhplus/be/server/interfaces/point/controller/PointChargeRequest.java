package kr.hhplus.be.server.interfaces.point.controller;


import kr.hhplus.be.server.domain.point.dto.PointCommand;
import kr.hhplus.be.server.domain.user.domain.User;

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
