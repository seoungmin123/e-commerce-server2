package kr.hhplus.be.server.domain.order.service;


import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.domain.order.domain.IOrderRepository;
import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.order.domain.OrderCompletedEvent;
import kr.hhplus.be.server.domain.order.domain.OrderItem;
import kr.hhplus.be.server.domain.order.dto.OrderCommand;
import kr.hhplus.be.server.domain.order.dto.OrderInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static kr.hhplus.be.server.common.exception.ApiErrorCode.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final IOrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;


    @Transactional
    public OrderInfo order(OrderCommand.Order command) {

        // 주문객체 생성
        Order order = Order.create(command.user() );

        // 주문 상품
        command.products().stream()
                .map(item -> OrderItem.create(item.product(), item.quantity()))
                .forEach(order::addOrderItem);

        order.calculateOrderAmounts();
        orderRepository.save(order);

        return OrderInfo.from(order);
    }

    @Transactional
    public OrderInfo confirm(OrderCommand.Confirm command) {
        Order order = orderRepository.findById(command.orderId()).orElseThrow(() -> new ApiException(NOT_FOUND));
        order.confirm();
        //외부전송 이벤트
        eventPublisher.publishEvent(OrderCompletedEvent.from(order));
        return OrderInfo.from(order);
    }

    // 쿠폰 사용
    @Transactional
    public OrderInfo applyCoupon(OrderCommand.ApplyCoupon command) {
        Order order = orderRepository.findById(command.orderId()).orElseThrow(() -> new ApiException(NOT_FOUND));
        order.applyCoupon(command.couponIssueId(), command.discountAmount());
        return OrderInfo.from(order);
    }
}
