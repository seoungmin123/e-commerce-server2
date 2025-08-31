package kr.hhplus.be.server.interfaces.point.controller;


import kr.hhplus.be.server.domain.point.dto.PointInfo;

import java.math.BigDecimal;

public record PointResponse(
        Long userId,
        String userName,
        BigDecimal point
) {
    public static PointResponse from(PointInfo response) {
        return new PointResponse(response.userId(), response.userName(), response.point());
    }
}
