package kr.hhplus.be.server.infra.point;


import kr.hhplus.be.server.point.domain.Point;
import kr.hhplus.be.server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointJpaRepository extends JpaRepository<Point, Long> {
    Point save(Point point);

    Optional<Point> findByUser(User user);

    Optional<Point> findByUserId(long id);
}
