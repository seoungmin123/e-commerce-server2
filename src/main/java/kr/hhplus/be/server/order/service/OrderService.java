package kr.hhplus.be.server.order.service;


import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.order.domain.IOrderRepository;
import kr.hhplus.be.server.order.domain.Order;
import kr.hhplus.be.server.order.domain.OrderItem;
import kr.hhplus.be.server.order.dto.OrderCommand;
import kr.hhplus.be.server.order.dto.OrderInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static kr.hhplus.be.server.common.exception.ApiErrorCode.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final IOrderRepository orderRepository;

    @Transactional
    public OrderInfo order(OrderCommand.Order command) {

        // 주문객체 생성
        Order order = Order.create(command.user() );

        // 주문 상품
        List<OrderItem> orderItems = command.products().stream()
                .map(item -> OrderItem.create(item.product(), item.quantity()))
                .toList();

        orderItems.forEach(order::addOrderItem);

        order.calculateOrderAmounts();
        orderRepository.save(order);

        return OrderInfo.from(order);
    }

    @Transactional
    public OrderInfo confirm(OrderCommand.Confirm command) {
        Order order = orderRepository.findById(command.orderId()).orElseThrow(() -> new ApiException(NOT_FOUND));
        order.confirm();
        return OrderInfo.from(order);
    }

    // 쿠폰 사용
    @Transactional
    public OrderInfo applyCoupon(Long orderId, Long couponIssueId, BigDecimal discountAmount) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ApiException(NOT_FOUND));
        order.applyCoupon(couponIssueId,discountAmount);
        return OrderInfo.from(order);
    }
}
