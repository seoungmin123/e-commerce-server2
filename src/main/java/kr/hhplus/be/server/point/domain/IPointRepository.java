package kr.hhplus.be.server.point.domain;


import kr.hhplus.be.server.user.domain.User;

import java.util.Optional;

public interface IPointRepository {
    Point save(Point point);

    Optional<Point> findByUser(User user);

    Optional<Point> findByUserWithLock(User user);
}
