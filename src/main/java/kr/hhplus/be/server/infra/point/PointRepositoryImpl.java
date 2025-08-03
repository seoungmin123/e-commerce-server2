package kr.hhplus.be.server.infra.point;


import kr.hhplus.be.server.point.domain.IPointRepository;
import kr.hhplus.be.server.point.domain.Point;
import kr.hhplus.be.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PointRepositoryImpl implements IPointRepository {
    private final PointJpaRepository pointJpaRepository;

    @Override
    public Point save(Point point) {
        return pointJpaRepository.save(point);
    }

    @Override
    public Optional<Point> findByUser(User user) {
        return pointJpaRepository.findByUser(user);
    }

    @Override
    public Optional<Point> findByUserId(long id) {
        return pointJpaRepository.findByUserId(id);
    }
}
