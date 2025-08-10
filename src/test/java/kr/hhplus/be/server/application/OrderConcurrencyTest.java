package kr.hhplus.be.server.application;

import kr.hhplus.be.server.DataBaseCleanUp;
import kr.hhplus.be.server.ServerApplication;
import kr.hhplus.be.server.application.order.OrderCriteria;
import kr.hhplus.be.server.application.order.OrderFacade;
import kr.hhplus.be.server.product.domain.IProductRepository;
import kr.hhplus.be.server.product.domain.ProductStock;
import kr.hhplus.be.server.user.domain.IUserRepository;
import kr.hhplus.be.server.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = ServerApplication.class)
@Testcontainers
public class OrderConcurrencyTest {

    private static final Logger log = LoggerFactory.getLogger(OrderConcurrencyTest.class);

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private DataBaseCleanUp dataBaseCleanUp;

    @BeforeEach
    public void setUp() {
        dataBaseCleanUp.execute();
    }

    @Test
    void 동일_사용자가_동시에_3번_주문_요청시_하나만_성공하고_2번은_ObjectOptimisticLockingFailureException_에러가_발생한다() throws InterruptedException {
        // given
        int threadCount = 3;
        Long userId = 1L;  // 포인트 100,000원 보유
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        OrderCriteria.Order criteria = new OrderCriteria.Order(
                user, List.of(new OrderCriteria.Item(1L, 1)), // 10,000원 상품 1개
                null
        );

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    orderFacade.order(criteria);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // then
        latch.await(5, TimeUnit.SECONDS);
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(2);

        executorService.shutdown();
    }

    @Test
    void 여러명의_사용자가_동시에_상품을_주문하여_재고가_소진되면_마지막_사용자의_주문은_실패하고_INSUFFICIENT_STOCK_예외가_발생한다() throws InterruptedException {
        // given
        int threadCount = 4;
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();


        // when
        for (int i = 0; i < threadCount; i++) {
            Long userId = (long) (i + 1);  // 각각 다른 사용자
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));
            // 상품3: 재고 10개, 가격 1원
            OrderCriteria.Order criteria = new OrderCriteria.Order(
                    user, List.of(new OrderCriteria.Item(3L, 3)), // 3개씩 구매 시도 (4명이 시도하면 총 12개 필요)
                    null
            );

            executorService.submit(() -> {
                try {
                    barrier.await();
                    orderFacade.order(criteria);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                  e.printStackTrace();
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // then
        latch.await(5, TimeUnit.SECONDS);

        ProductStock afterStock = productRepository.findByProductId(3L)
                .orElseThrow(() -> new RuntimeException("재고 정보가 없습니다."));

        assertThat(afterStock.getQuantity()).isEqualTo(1);  // 10개 중 9개가 소진됨
        assertThat(successCount.get()).isEqualTo(3);  // 3명만 성공 (9개 구매)
        assertThat(failCount.get()).isEqualTo(1);     // 1명 실패 (재고 부족)

        executorService.shutdown();
    }

}
