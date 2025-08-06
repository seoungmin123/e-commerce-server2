package kr.hhplus.be.server.point.service;


import kr.hhplus.be.server.common.exception.ApiErrorCode;
import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.point.domain.IPointRepository;
import kr.hhplus.be.server.point.domain.Point;
import kr.hhplus.be.server.point.dto.PointCommand;
import kr.hhplus.be.server.point.dto.PointInfo;
import kr.hhplus.be.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static kr.hhplus.be.server.common.exception.ApiErrorCode.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PointService {
    private final IPointRepository pointRepository;

    // 포인트 조회
    @Transactional(readOnly = true)
    public PointInfo getPoint(User user) {
        Point point = pointRepository.findByUser(user).orElseGet(() -> Point.create(user));
        return PointInfo.from(point);
    }

    // 포인트 충전
    @Transactional
    public PointInfo charge(PointCommand.Charge command) {
        try{
            // DB에서 사용자의 포인트 정보를 읽어옴 (OPTIMISTIC)
            Point point = pointRepository.findByUserWithLock(command.user()).orElseGet(()
                    -> pointRepository.save(Point.create(command.user())));
            point.charge(command.amount());
            point = pointRepository.save(point);
            return PointInfo.from(point);

        } catch (ObjectOptimisticLockingFailureException e) {
            //fixme 재시도 로직없음 - 실패시 이미처리되었습니다 문구
            throw new ApiException(ApiErrorCode.CONFLICT);
        }
    }

    // 포인트 사용
    @Transactional
    public void use(PointCommand.Use command) {
        try{
            // DB에서 사용자의 포인트 정보를 읽어옴 (OPTIMISTIC)
            Point point = pointRepository.findByUserWithLock(command.user()).orElseThrow(()
                    -> new ApiException(NOT_FOUND));
            point.use(command.amount());
            pointRepository.save(point);
        } catch (ObjectOptimisticLockingFailureException e) {
            //fixme 재시도 로직없음 - 실패시 중복결제입니다 다시결재요청 문구
            throw new ApiException(ApiErrorCode.CONFLICT);
        }
    }
}
