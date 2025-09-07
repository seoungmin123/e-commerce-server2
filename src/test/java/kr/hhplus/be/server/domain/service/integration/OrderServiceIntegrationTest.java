package kr.hhplus.be.server.domain.service.integration;


import kr.hhplus.be.server.ServerApplication;
import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.domain.order.domain.IOrderRepository;
import kr.hhplus.be.server.domain.order.domain.Order;
import kr.hhplus.be.server.domain.order.domain.OrderOutbox;
import kr.hhplus.be.server.domain.order.dto.OrderCommand;
import kr.hhplus.be.server.domain.order.dto.OrderInfo;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.product.domain.IProductRepository;
import kr.hhplus.be.server.domain.product.domain.Product;
import kr.hhplus.be.server.domain.user.domain.IUserRepository;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.infra.outbox.OrderOutBoxRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static kr.hhplus.be.server.common.exception.ApiErrorCode.NOT_FOUND;
import static kr.hhplus.be.server.domain.order.domain.Order.OrderStatus.PAID;
import static kr.hhplus.be.server.domain.order.domain.OrderOutbox.OutboxStatus.INIT;
import static kr.hhplus.be.server.domain.order.domain.OrderOutbox.OutboxStatus.PUBLISHED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = ServerApplication.class)
@Testcontainers
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IOrderRepository orderRepository;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private OrderOutBoxRepository orderOutBoxRepository;

    @Test
    void 주문_생성시_주문정보가_정상적으로_생성된다() {
        // given
        User user = userRepository.save(User.create("테스트유저"));

        // test-data.sql
        Product product = productRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));

        OrderCommand.Order command = new OrderCommand.Order(user, List.of(new OrderCommand.Item(product.getId(), product, 10)), null);

        // when
        OrderInfo orderInfo = orderService.order(command);

        // then
        assertThat(orderInfo).isNotNull();
        assertThat(orderInfo.status()).isEqualTo("결제 대기");
        assertThat(orderInfo.paymentAmount()).isEqualTo(BigDecimal.valueOf(100000).setScale(2));
        assertThat(orderInfo.totalAmount()).isEqualTo(BigDecimal.valueOf(100000).setScale(2));

        Order savedOrder = orderRepository.findById(orderInfo.orderId())
                .orElseThrow(() -> new RuntimeException("주문이 저장되지 않았습니다."));
        assertThat(savedOrder.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
    }

    @Test
    void 쿠폰_적용시_할인금액이_정상적으로_반영된다() {
        // given
        User user = userRepository.save(User.create("테스트유저"));
        Product product = productRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));

        OrderCommand.Order command = new OrderCommand.Order(user, List.of(new OrderCommand.Item(product.getId(), product, 5)), null);
        OrderInfo orderInfo = orderService.order(command);

        // when
        OrderInfo discountedOrder = orderService.applyCoupon(
                new OrderCommand.ApplyCoupon(
                        orderInfo.orderId(),
                        1L,
                        BigDecimal.valueOf(5000))
        );

        // then
        assertThat(discountedOrder).isNotNull();
        assertThat(discountedOrder.totalAmount()).isEqualTo(BigDecimal.valueOf(50000).setScale(2));
        assertThat(discountedOrder.paymentAmount())
                .isEqualTo(BigDecimal.valueOf(45000).setScale(2));
    }
    @Test
    void 주문_확정시_상태가_변경되고_이벤트가_발행된다() {
        // given
        User user = userRepository.save(User.create("테스트유저"));
        Product product = productRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));

        OrderCommand.Order command = new OrderCommand.Order(user, List.of(new OrderCommand.Item(product.getId(), product, 1)), null);
        OrderInfo orderInfo = orderService.order(command);

        // when
        OrderInfo confirmedOrder = orderService.confirm(
                new OrderCommand.Confirm(orderInfo.orderId())
        );

        // then
        // 1. 주문 상태가 변경되었는지 확인
        assertThat(confirmedOrder).isNotNull();
        assertThat(confirmedOrder.status()).isEqualTo("결제 완료");

        Order savedOrder = orderRepository.findById(confirmedOrder.orderId())
                .orElseThrow(() -> new RuntimeException("주문이 존재하지 않습니다."));
        assertThat(savedOrder.getStatus()).isEqualTo(PAID);

        // 2. Outbox 저장 확인
        OrderOutbox savedOutbox = orderOutBoxRepository.findByOrderId(confirmedOrder.orderId())
                .orElseThrow(() -> new RuntimeException("Outbox에 이벤트가 저장되지 않았습니다."));

        assertThat(savedOutbox.getEventType()).isEqualTo("Completed");
        assertThat(savedOutbox.getOrderId()).isEqualTo(confirmedOrder.orderId());
        assertThat(savedOutbox.getStatus()).isEqualTo(INIT); // 아직 발행되지 않은 상태

        // 3. kafka 이벤트 발행 확인
        await()
                .atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    OrderOutbox updatedOutbox = orderOutBoxRepository.findByOrderId(confirmedOrder.orderId()).get();
                    assertThat(updatedOutbox.getStatus()).isEqualTo(PUBLISHED); // 발행된 상태
                });
    }

    @Test
    void 존재하지_않는_주문_확정시_NOT_FOUND_예외가_발생한다() {
        // given
        Long nonExistentOrderId = 999L;

        // when then
        assertThatThrownBy(() ->
                orderService.confirm(new OrderCommand.Confirm(nonExistentOrderId))
        )
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiErrorCode", NOT_FOUND);
    }

    @Test
    void 존재하지_않는_주문에_쿠폰_적용시_NOT_FOUND_예외가_발생한다() {
        // given
        Long nonExistentOrderId = 999L;
        when(orderRepository.findById(nonExistentOrderId)).thenReturn(Optional.empty());

        // when then
        assertThatThrownBy(() ->
                orderService.applyCoupon(new OrderCommand.ApplyCoupon(nonExistentOrderId, 1L, BigDecimal.valueOf(5000))))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiErrorCode", NOT_FOUND);
    }
}