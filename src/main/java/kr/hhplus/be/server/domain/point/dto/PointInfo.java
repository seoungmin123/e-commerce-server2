package kr.hhplus.be.server.domain.point.dto;

import kr.hhplus.be.server.domain.point.domain.Point;

import java.math.BigDecimal;

public record PointInfo(
        Long userId,
        String userName,
        BigDecimal point) {
    public static PointInfo from(Point point) {
        return new PointInfo(
                point.getUser().getId(),
                point.getUser().getName(),
                point.getPoint()
        );
    }
}
