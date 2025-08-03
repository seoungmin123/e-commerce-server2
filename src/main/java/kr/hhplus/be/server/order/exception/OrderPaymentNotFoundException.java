package kr.hhplus.be.server.order.exception;

import kr.hhplus.be.server.common.exception.NotFoundException;

public class OrderPaymentNotFoundException extends NotFoundException {
    public OrderPaymentNotFoundException(Long orderId) {
        super("해당 주문의 결제 정보를 찾을 수 없습니다. orderId=" + orderId);
    }
}