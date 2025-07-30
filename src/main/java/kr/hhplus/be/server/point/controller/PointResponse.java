package kr.hhplus.be.server.point.controller;


import kr.hhplus.be.server.point.dto.PointInfo;

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
