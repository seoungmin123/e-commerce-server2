package kr.hhplus.be.server.application.order;


import kr.hhplus.be.server.coupon.dto.CouponDiscountInfo;
import kr.hhplus.be.server.coupon.service.CouponService;
import kr.hhplus.be.server.order.dto.OrderCommand;
import kr.hhplus.be.server.order.dto.OrderInfo;
import kr.hhplus.be.server.order.service.OrderService;
import kr.hhplus.be.server.payment.dto.PaymentCommand;
import kr.hhplus.be.server.payment.service.PaymentService;
import kr.hhplus.be.server.point.dto.PointCommand;
import kr.hhplus.be.server.point.service.PointService;
import kr.hhplus.be.server.product.dto.ValidatedProductInfo;
import kr.hhplus.be.server.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderFacade {
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final ProductService productService;
    private final PointService pointService;
    private final CouponService couponService;

    @Transactional
    public OrderResult order(OrderCriteria.Order criteria) {
        OrderCommand.Order orderCommand = criteria.toCommand();

        // 상품 검증
        List<ValidatedProductInfo> validateProducts = productService.validateProducts(orderCommand.products());

        // 주문 생성
        orderCommand = orderCommand.with(validateProducts);
        OrderInfo orderInfo = orderService.order(orderCommand);

        // 쿠폰 사용 및 할인 적용
        CouponDiscountInfo discountInfo = couponService.use(criteria.user(), criteria.couponIssueId(), orderInfo.totalAmount());
        orderInfo = orderService.applyCoupon(OrderCommand.ApplyCoupon.of(orderInfo.orderId(), criteria.couponIssueId(), discountInfo.discountAmount()));

        // 결제, 포인트 차감, 재고 차감
        paymentService.pay(PaymentCommand.Pay.of(criteria.user(), orderInfo.orderId(), orderInfo.paymentAmount()));
        pointService.use(PointCommand.Use.of(criteria.user(), orderInfo.paymentAmount()));
        productService.deductStock(orderCommand.products());

        // 주문 확정
        orderInfo = orderService.confirm(OrderCommand.Confirm.from(orderInfo.orderId()));

        return OrderResult.from(orderInfo);
    }
}
