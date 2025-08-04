package kr.hhplus.be.server.coupon.service;

import kr.hhplus.be.server.common.exception.ApiErrorCode;
import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.coupon.domain.Coupon;
import kr.hhplus.be.server.coupon.domain.CouponIssue;
import kr.hhplus.be.server.coupon.domain.ICouponRepository;
import kr.hhplus.be.server.coupon.dto.CouponCommand;
import kr.hhplus.be.server.coupon.dto.CouponDiscountInfo;
import kr.hhplus.be.server.coupon.dto.CouponInfo;
import kr.hhplus.be.server.user.domain.User;
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

    // 쿠폰 발급
    @Transactional
    public CouponInfo issueCoupon(CouponCommand.Issue command) {
        // DB에서 쿠폰 정보를 읽어옴 (PESSIMISTIC_WRITE)
        Coupon coupon = couponRepository.findByIdWithLock(command.couponId()).orElseThrow(() -> new ApiException(NOT_FOUND));

        CouponIssue couponIssue = coupon.issue(command.user());
        try {
            couponIssue = couponRepository.save(couponIssue);
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(ApiErrorCode.CONFLICT);
        }
        return CouponInfo.from(couponIssue);
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
