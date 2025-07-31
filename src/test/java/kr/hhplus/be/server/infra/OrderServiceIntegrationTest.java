package kr.hhplus.be.server.infra;


import kr.hhplus.be.server.order.dto.OrderCreateCommand;
import kr.hhplus.be.server.order.dto.OrderInfo;
import kr.hhplus.be.server.order.service.OrderService;
import kr.hhplus.be.server.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = {"file:./init/01-cleanup.sql", "file:./init/03-test-data.sql"})
class OrderServiceIntegrationTest {
    @Autowired
    private OrderService orderService;

    @Test
    void 주문생성시_재고가_정상적으로_차감되고_주문정보가_생성된다() {
        // given
        User user = User.create("테스트유저");
        OrderCreateCommand command = new OrderCreateCommand(user,
                List.of(new OrderCreateCommand.OrderItemCommand(1L, null,10)),  // 테스트상품1 10개
                null  // 쿠폰 미사용
        );

        // when
        OrderInfo orderInfo = orderService.order(command);

        // then
        assertThat(orderInfo)
                .isNotNull()
                .satisfies(info -> {
                    assertThat(info.status()).isEqualTo("결제 대기");
                    assertThat(info.totalAmount()).isEqualTo(BigDecimal.valueOf(100000).setScale(2));  // 10개 * 10000원
                });
    }

    @Test
    void 주문생성시_쿠폰할인이_정상적으로_적용된다() {
        // given
        User user = User.create("테스트유저");
        ReflectionTestUtils.setField(user, "id", 1L);

        OrderCreateCommand command = new OrderCreateCommand(user,
                List.of(new OrderCreateCommand.OrderItemCommand(1L, null,5)),  // 테스트상품1 5개
                3L );

        // when
        OrderInfo orderInfo = orderService.order(command);

        // then
        assertThat(orderInfo)
                .isNotNull()
                .satisfies(info -> {
                    assertThat(info.totalAmount()).isEqualTo(BigDecimal.valueOf(45000).setScale(2));  // 5개 * 10000원 - 5000원
                });
    }
}
