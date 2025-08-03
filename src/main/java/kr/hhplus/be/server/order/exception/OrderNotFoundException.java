package kr.hhplus.be.server.order.exception;

import kr.hhplus.be.server.common.exception.NotFoundException;

public class OrderNotFoundException extends NotFoundException {
    public OrderNotFoundException(Long orderId) {
        super("해당 주문을 찾을 수 없습니다. orderId : " + orderId);
    }
}