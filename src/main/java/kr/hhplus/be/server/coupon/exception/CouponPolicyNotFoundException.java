package kr.hhplus.be.server.coupon.exception;

import kr.hhplus.be.server.common.exception.NotFoundException;

public class CouponPolicyNotFoundException extends NotFoundException {
    public CouponPolicyNotFoundException(Long CouponPolicy) {
        super("존재하지 않는 쿠폰 정책입니다. CouponPolicy ID: " + CouponPolicy);
    }
}