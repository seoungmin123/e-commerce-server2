package kr.hhplus.be.server.coupon.domain;


import jakarta.persistence.*;
import kr.hhplus.be.server.common.domain.BaseEntity;
import kr.hhplus.be.server.common.exception.ApiErrorCode;
import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.user.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;
import static kr.hhplus.be.server.coupon.domain.CouponIssue.CouponStatus.*;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Table(name = "coupon_issue",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "coupon_id"})
        })
@NoArgsConstructor(access = PROTECTED)
public class CouponIssue extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "coupon_id", nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private Coupon coupon;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    private CouponIssue(User user, Coupon coupon, LocalDateTime expiredAt) {
        this.user = user;
        this.coupon = coupon;
        this.expiredAt = expiredAt;
    }

    public static CouponIssue create(User user, Coupon coupon) {
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(coupon.getValidityPeriod());
        return new CouponIssue(user, coupon, expiredAt);
    }

    // 쿠폰 할인계산
    public BigDecimal calculateDiscountAmount(BigDecimal orderAmount) {
        if (coupon.getDiscountType() == Coupon.DiscountType.FIXED) { //정액계산
            return coupon.getDiscountValue();
        } else {  // 퍼센트 계산
            return orderAmount.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100));
        }
    }

    // 쿠폰 사용
    public void use(User user) {
        validateUseable(user);
        this.usedAt = LocalDateTime.now();
    }

    // 쿠폰 유효성 검증
    public void validateUseable(User user) {
        if (usedAt != null) { //사용여부 확인
            throw new ApiException(ApiErrorCode.INVALID_REQUEST);
        }
        if (expiredAt.isBefore(LocalDateTime.now())) { // 만료여부 확인
            throw new ApiException(ApiErrorCode.INVALID_REQUEST);
        }
        if (!this.user.getId().equals(user.getId())) { //사용자 확인
            throw new ApiException(ApiErrorCode.INVALID_REQUEST);
        }
    }

    public String getStatus() {
        if (usedAt != null) {
            return USED.getDescription();
        }
        if (expiredAt.isBefore(LocalDateTime.now())) {
            return EXPIRED.getDescription();
        }
        return UNUSED.getDescription();
    }


    public enum CouponStatus {
        UNUSED("미사용"),
        USED("사용 완료"),
        EXPIRED("기간 만료");

        private final String description;

        CouponStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
