package kr.hhplus.be.server.infra;


import kr.hhplus.be.server.DataBaseCleanUp;
import kr.hhplus.be.server.ServerApplication;
import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.order.domain.IOrderRepository;
import kr.hhplus.be.server.order.domain.Order;
import kr.hhplus.be.server.order.dto.OrderCommand;
import kr.hhplus.be.server.order.dto.OrderInfo;
import kr.hhplus.be.server.order.service.OrderService;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.user.domain.User;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static kr.hhplus.be.server.common.exception.ApiErrorCode.NOT_FOUND;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = ServerApplication.class)
@Testcontainers
class OrderServiceIntegrationTest {
    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    @Autowired
    private DataBaseCleanUp dataBaseCleanUp;

    @BeforeEach
    public void setUp() {
        dataBaseCleanUp.execute();
    }

    @Test
    void 주문_생성_후_주문금액이_계산된다() {
        // given
        User user = mock(User.class);
        Product product = Product.create("테스트상품", BigDecimal.valueOf(10000));
        ReflectionTestUtils.setField(product, "id", 1L);
        OrderCommand.Order command = new OrderCommand.Order(user,
                List.of(new OrderCommand.Item(1L, product, 2)), null
        );

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        OrderInfo result = orderService.order(command);

        // then
        AssertionsForClassTypes.assertThat(result.totalAmount()).isEqualTo(BigDecimal.valueOf(23000)); // 배송비 포함
        verify(orderRepository).save(any(Order.class));
    }


    @Test
    void 존재하지_않는_주문_확정시_NOT_FOUND_예외가_발생한다() {
        // given
        Long nonExistentOrderId = 999L;
        when(orderRepository.findById(nonExistentOrderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.confirm(new OrderCommand.Confirm(nonExistentOrderId)))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiErrorCode", NOT_FOUND);

        verify(eventPublisher, never()).publishEvent(any());
    }


    @Test
    void 존재하지_않는_주문에_쿠폰_적용시_NOT_FOUND_예외가_발생한다() {
        // given
        Long nonExistentOrderId = 999L;
        when(orderRepository.findById(nonExistentOrderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                orderService.applyCoupon(new OrderCommand.ApplyCoupon(nonExistentOrderId, 1L, BigDecimal.valueOf(5000))))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiErrorCode", NOT_FOUND);
    }
}