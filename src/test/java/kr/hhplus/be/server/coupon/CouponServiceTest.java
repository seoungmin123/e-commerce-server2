package kr.hhplus.be.server.coupon;


import kr.hhplus.be.server.common.exception.ApiErrorCode;
import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.coupon.domain.Coupon;
import kr.hhplus.be.server.coupon.domain.CouponIssue;
import kr.hhplus.be.server.coupon.domain.ICouponRepository;
import kr.hhplus.be.server.coupon.dto.CouponCommand;
import kr.hhplus.be.server.coupon.dto.CouponDiscountInfo;
import kr.hhplus.be.server.coupon.service.CouponService;
import kr.hhplus.be.server.user.domain.User;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;

import static kr.hhplus.be.server.common.exception.ApiErrorCode.NOT_FOUND;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class CouponServiceTest {
    @Mock
    private ICouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    @Test
    void 존재하지_않는_쿠폰_발급시_NOT_FOUND_예외가_발생한다() {
        // given
        User user = mock(User.class);
        Long couponId = 999L;
        CouponCommand.Issue command = new CouponCommand.Issue(user, couponId);

        when(couponRepository.findByIdWithLock(couponId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponService.issueCoupon(command))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiErrorCode", NOT_FOUND);
        verify(couponRepository).findByIdWithLock(couponId);
        verify(couponRepository, never()).save(any(CouponIssue.class));
    }

    @Test
    void 쿠폰발급_시_DB_제약조건_위반으로_CONFLICT_예외가_발생한다() {
        // given
        User user = mock(User.class);
        Long couponId = 1L;
        CouponCommand.Issue command = new CouponCommand.Issue(user,couponId);

        Coupon coupon = mock(Coupon.class);
        CouponIssue couponIssue = mock(CouponIssue.class);

        when(couponRepository.findByIdWithLock(couponId)).thenReturn(Optional.of(coupon));
        when(coupon.issue(user)).thenReturn(couponIssue);
        when(couponRepository.save(any(CouponIssue.class)))
                .thenThrow(new DataIntegrityViolationException("중복 쿠폰 발급"));

        // when & then
        assertThatThrownBy(() -> couponService.issueCoupon(command))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiErrorCode", ApiErrorCode.CONFLICT);

        verify(couponRepository).findByIdWithLock(couponId);
        verify(coupon).issue(user);
        verify(couponRepository).save(couponIssue);
    }


    @Test
    void 쿠폰_사용시_할인_금액이_정상적으로_계산된다() {
        // given
        User user = mock(User.class);
        Long couponIssueId = 1L;
        BigDecimal totalAmount = BigDecimal.valueOf(10000);
        BigDecimal expectedDiscountAmount = BigDecimal.valueOf(1000);

        CouponIssue couponIssue = mock(CouponIssue.class);
        when(couponIssue.getId()).thenReturn(couponIssueId);
        when(couponIssue.calculateDiscountAmount(totalAmount)).thenReturn(expectedDiscountAmount);
        when(couponRepository.findByCouponIssueId(couponIssueId)).thenReturn(Optional.of(couponIssue));

        // when
        CouponDiscountInfo result = couponService.use(user, couponIssueId, totalAmount);

        // then
        AssertionsForClassTypes.assertThat(result.couponIssueId()).isEqualTo(couponIssueId);
        AssertionsForClassTypes.assertThat(result.discountAmount()).isEqualTo(expectedDiscountAmount);
        verify(couponIssue).use(user);
    }

    @Test
    void 쿠폰ID가_null이면_할인없이_정상처리된다() {
        // given
        User user = mock(User.class);
        BigDecimal totalAmount = BigDecimal.valueOf(10000);

        // when
        CouponDiscountInfo result = couponService.use(user, null, totalAmount);

        // then
        AssertionsForClassTypes.assertThat(result.couponIssueId()).isNull();
        AssertionsForClassTypes.assertThat(result.discountAmount()).isEqualTo(BigDecimal.ZERO);
        verifyNoInteractions(couponRepository);
    }
}