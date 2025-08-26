package kr.hhplus.be.server.coupon.domain;

import kr.hhplus.be.server.user.domain.User;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ICouponRepository {
    Optional<Coupon> findById(Long aLong);

    Optional<Coupon> findByIdWithLock(Long aLong);

    CouponIssue save(CouponIssue couponIssue);

    List<CouponIssue> findAllByUser(User user);

    Optional<CouponIssue> findByCouponIssueId(Long couponIssueId);

    boolean isIssuedMember(Long couponId, Long userId);

    boolean addRequest(Long couponId, Long userId);

    List<Coupon> findIssuableCoupons(LocalDateTime now);

    Set<ZSetOperations.TypedTuple<Long>> getRequestsByTimestamp(Long id, Integer issuableCount);

    void removeRequests(Long id, Integer issuableCount);

    int getRequestCount(Long id);

    void addIssuance(Long id, List<Long> successfulUserIds);

    void addFailures(Long id, List<Long> failedUserIds);

    int getIssuedCount(Long id);

    int getFailedCount(Long id);
}
