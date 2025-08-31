package kr.hhplus.be.server.domain.coupon.service;

import kr.hhplus.be.server.common.exception.ApiErrorCode;
import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.common.redisson.DistributedLock;
import kr.hhplus.be.server.domain.coupon.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.domain.CouponIssue;
import kr.hhplus.be.server.domain.coupon.domain.ICouponRepository;
import kr.hhplus.be.server.domain.coupon.dto.CouponCommand;
import kr.hhplus.be.server.domain.coupon.dto.CouponDiscountInfo;
import kr.hhplus.be.server.domain.coupon.dto.CouponInfo;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static kr.hhplus.be.server.common.exception.ApiErrorCode.NOT_FOUND;


@Service
@RequiredArgsConstructor
public class CouponService {
    private final ICouponRepository couponRepository;

    @Transactional
    @DistributedLock(
            topic = "coupon",
            keyExpression = "#command.couponId",
            waitTime = 5,
            leaseTime = 3
    )
    public CouponInfo issueCoupon(CouponCommand.Issue command) {
        Coupon coupon = couponRepository.findById(command.couponId()).orElseThrow(() -> new ApiException(NOT_FOUND));

        CouponIssue couponIssue = coupon.issue(command.user());
        try {
            couponIssue = couponRepository.save(couponIssue);
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(ApiErrorCode.CONFLICT);
        }
        return CouponInfo.from(couponIssue);
    }

    @Transactional
    public boolean requestConponIssue(CouponCommand.Issue command) {
        Coupon coupon = couponRepository.findById(command.couponId()).orElseThrow(() -> new ApiException(NOT_FOUND));
        coupon.validateIssuable();

        if (couponRepository.isIssuedMember(command.couponId(), command.user().getId())) {
            throw new ApiException(ApiErrorCode.CONFLICT);
        }

        return couponRepository.addRequest(command.couponId(), command.user().getId());
    }


    //쿠폰 조회 목록
    @Transactional(readOnly = true)
    public List<CouponInfo> getCoupons(User user) {
        List<CouponIssue> couponIssues = couponRepository.findAllByUser(user);
        return couponIssues.stream()
                .map(CouponInfo::from)
                .toList();
    }

    //쿠폰 사용
    @Transactional
    public CouponDiscountInfo use(User user, Long couponIssueId, BigDecimal totalAmount) {
        // 쿠폰 미존재시 미적용
        if (couponIssueId == null) {
            return new CouponDiscountInfo(null, BigDecimal.ZERO);
        }

        CouponIssue couponIssue = couponRepository.findByCouponIssueId(couponIssueId).orElseThrow(() -> new ApiException(NOT_FOUND));
        BigDecimal discountAmount = couponIssue.calculateDiscountAmount(totalAmount); //할인금액 계산
        couponIssue.use(user);
        return new CouponDiscountInfo(couponIssue.getId() , discountAmount);
    }
}
