package kr.hhplus.be.server.coupon.exception;

import kr.hhplus.be.server.common.exception.NotFoundException;


public class CouponNotFoundException extends NotFoundException {
    public CouponNotFoundException(Long userId, Long couponId) {
        super("존재하지않는 쿠폰입니다. couponId : " + couponId +" userId: "+ userId);
    }
}