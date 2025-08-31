package kr.hhplus.be.server.domain.point.domain;


import kr.hhplus.be.server.domain.user.domain.User;

import java.util.Optional;

public interface IPointRepository {
    Point save(Point point);

    Optional<Point> findByUser(User user);

    Optional<Point> findByUserWithLock(User user);
}
