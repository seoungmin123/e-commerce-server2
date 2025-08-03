package kr.hhplus.be.server.coupon.domain;

import kr.hhplus.be.server.user.domain.User;

import java.util.List;
import java.util.Optional;

public interface ICouponRepository {
    Optional<Coupon> findById(Long aLong);

    Optional<Coupon> findByIdWithLock(Long aLong);

    CouponIssue save(CouponIssue couponIssue);

    List<CouponIssue> findAllByUser(User user);

    Optional<CouponIssue> findByCouponIssueId(Long couponIssueId);
}
