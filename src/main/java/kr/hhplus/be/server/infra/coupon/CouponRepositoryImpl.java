package kr.hhplus.be.server.infra.coupon;


import kr.hhplus.be.server.coupon.domain.Coupon;
import kr.hhplus.be.server.coupon.domain.CouponIssue;
import kr.hhplus.be.server.coupon.domain.ICouponRepository;
import kr.hhplus.be.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements ICouponRepository {
    private final CouponJpaRepository couponJpaRepository;
    private final CouponIssueJpaRepository couponIssueJpaRepository;

    @Override
    public Optional<Coupon> findById(Long id) {
        return couponJpaRepository.findById(id);
    }

    // 비관적 쓰기락 적용
    @Override
    public Optional<Coupon> findByIdWithLock(Long id) {
        return couponJpaRepository.findByIdWithLock(id);
    }

    @Override
    public CouponIssue save(CouponIssue couponIssue) {
        return couponIssueJpaRepository.save(couponIssue);
    }

    @Override
    public List<CouponIssue> findAllByUser(User user) {
        return couponIssueJpaRepository.findAllByUser(user);
    }

    @Override
    public Optional<CouponIssue> findByCouponIssueId(Long id) {
        return couponIssueJpaRepository.findById(id);
    }
}
