package kr.hhplus.be.server.order;


import kr.hhplus.be.server.common.exception.ApiErrorCode;
import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.order.domain.Order;
import kr.hhplus.be.server.order.domain.OrderItem;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.user.domain.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {


    @Test
    void 여러개의_주문상품의_총_금액이_정확히_계산된다() {
        // given
        User user = createUser();
        List<OrderItem> orderItems = List.of(
                createOrderItem("상품1", 10000, 2),
                createOrderItem("상품2", 20000, 1)
        );

        // when
        Order order = Order.create(user);
        orderItems.forEach(order::addOrderItem);
        order.calculateOrderAmounts();

        // then
        assertThat(order.getItemAmount()).isEqualTo(BigDecimal.valueOf(40000));
        assertThat(order.getTotalAmount()).isEqualTo(BigDecimal.valueOf(40000));
    }

    @Test
    void 주문확정시_결제대기_상태가_아니면_INVALID_REQUEST_예외가_발생한다() {
        // given
        User user = createUser();
        List<OrderItem> orderItems = createOrderItems();
        Order order = Order.create(user);
        orderItems.forEach(order::addOrderItem);
        order.confirm();

        // when & then
        assertThatThrownBy(order::confirm)
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiErrorCode", ApiErrorCode.INVALID_REQUEST);
    }

    @Test
    void 주문확정되면_주문의_상태가_결제_완료로_변경된다() {
        // given
        User user = createUser();
        List<OrderItem> orderItems = createOrderItems();
        Order order = Order.create(user);
        orderItems.forEach(order::addOrderItem);

        // when
        order.confirm();

        // then
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.PAID);
    }

    private User createUser() {
        return User.create("연예진");
    }

    private OrderItem createOrderItem(String name, int price, int quantity) {
        Product product = Product.create(name, BigDecimal.valueOf(price));
        return OrderItem.create(product, quantity);
    }

    private List<OrderItem> createOrderItems() {
        return List.of(createOrderItem("테스트상품", 10000, 1));
    }
}