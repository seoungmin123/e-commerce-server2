package kr.hhplus.be.server.order.exception;

import kr.hhplus.be.server.common.response.ApiResponseCode;
import kr.hhplus.be.server.order.domain.Order;

public class InvalidOrderStatusTransitionException extends OrderException {
    public InvalidOrderStatusTransitionException(Order.OrderStatus current, String attemptedAction) {
        super(
                ApiResponseCode.FAIL_ORDER_INVALID_STATUS_TRANSITION_400,
                "현재 상태가 [" + current + "]이므로 " + attemptedAction + "할 수 없습니다."
        );
    }
}