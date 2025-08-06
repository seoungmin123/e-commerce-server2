package kr.hhplus.be.server.application;


import kr.hhplus.be.server.DataBaseCleanUp;
import kr.hhplus.be.server.ServerApplication;
import kr.hhplus.be.server.application.order.OrderCriteria;
import kr.hhplus.be.server.application.order.OrderFacade;
import kr.hhplus.be.server.application.order.OrderResult;
import kr.hhplus.be.server.common.exception.ApiErrorCode;
import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.point.domain.IPointRepository;
import kr.hhplus.be.server.point.domain.Point;
import kr.hhplus.be.server.product.domain.IProductRepository;
import kr.hhplus.be.server.product.domain.ProductStock;
import kr.hhplus.be.server.user.domain.IUserRepository;
import kr.hhplus.be.server.user.domain.User;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static kr.hhplus.be.server.common.exception.ApiErrorCode.INVALID_REQUEST;

@SpringBootTest(classes = ServerApplication.class)
@Testcontainers
class OrderFacadeIntegrationTest {
    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private IPointRepository pointRepository;

    @Autowired
    private DataBaseCleanUp dataBaseCleanUp;

    @BeforeEach
    public void setUp() {
        dataBaseCleanUp.execute();
    }
    @Test
    @Transactional
    void 주문생성_결제_쿠폰적용_포인트사용_재고차감이_정상적으로_처리된다() {
        // given
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));

        OrderCriteria.Order criteria = new OrderCriteria.Order(
                user, List.of(new OrderCriteria.Item(1L, 1)),  // 테스트상품1 1개
                4L  // 10% 할인 쿠폰
        );

        ProductStock beforeStock = productRepository.findByProductId(1L)
                .orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));
        int initialStock = beforeStock.getQuantity();
        Point point = pointRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("포인트 정보가 없습니다."));
        BigDecimal initialPoint = point.getPoint();

        // when
        OrderResult result = orderFacade.order(criteria);

        // then
        // 주문 결과
        AssertionsForClassTypes.assertThat(result).isNotNull();
        AssertionsForClassTypes.assertThat(result.status()).isEqualTo("결제 완료");
        AssertionsForClassTypes.assertThat(result.totalAmount()).isEqualTo(BigDecimal.valueOf(13000).setScale(2));  // 상품가격+배송비
        AssertionsForClassTypes.assertThat(result.paymentAmount()).isEqualTo(BigDecimal.valueOf(11700).setScale(2));  // 10% 할인 적용

        // 재고 차감
        ProductStock afterStock = productRepository.findByProductId(1L)
                .orElseThrow(() -> new RuntimeException("재고 정보가 없습니다."));
        AssertionsForClassTypes.assertThat(afterStock.getQuantity()).isEqualTo(initialStock - 1);

        // 포인트 차감
        AssertionsForClassTypes.assertThat(point.getPoint().setScale(2))
                .isEqualTo(initialPoint.subtract(result.paymentAmount()));
    }

    @Test
    void 잔액이_부족한_경우_INVALID_REQUEST_예외가_발생하여_주문이_실패하고_재고가_차감되지_않는다() {
        // given
        User user = userRepository.findById(5L)  // 포인트가 없는 사용자
                .orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));

        OrderCriteria.Order criteria = new OrderCriteria.Order(
                user, List.of(new OrderCriteria.Item(1L, 10)),
                null  // 쿠폰 미사용
        );

        ProductStock beforeStock = productRepository.findByProductId(1L)
                .orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));
        int initialStock = beforeStock.getQuantity();

        // when & then
        AssertionsForClassTypes.assertThatThrownBy(() -> orderFacade.order(criteria))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiErrorCode", INVALID_REQUEST);

        ProductStock afterStock = productRepository.findByProductId(1L)
                .orElseThrow(() -> new RuntimeException("재고 정보가 없습니다."));
        AssertionsForClassTypes.assertThat(afterStock.getQuantity()).isEqualTo(initialStock);
    }

    @Test
    void 재고가_부족한_경우_INSUFFICIENT_STOCK_예외가_발생하여_주문이_실패하고_포인트가_차감되지_않는다() {
        // given
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));

        Point point = pointRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("포인트 정보가 없습니다."));
        BigDecimal initialPoint = point.getPoint();

        OrderCriteria.Order criteria = new OrderCriteria.Order(
                user, List.of(new OrderCriteria.Item(3L, 999)),  // 재고보다 많은 수량
                null
        );

        // when & then
        AssertionsForClassTypes.assertThatThrownBy(() -> orderFacade.order(criteria))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiErrorCode", ApiErrorCode.INSUFFICIENT_STOCK);

        AssertionsForClassTypes.assertThat(point.getPoint()).isEqualTo(initialPoint);  // 트랜잭션 롤백으로 초기값 유지
    }
}