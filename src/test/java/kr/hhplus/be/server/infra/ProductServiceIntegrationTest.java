package kr.hhplus.be.server.infra;

import kr.hhplus.be.server.DataBaseCleanUp;
import kr.hhplus.be.server.ServerApplication;
import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.order.dto.OrderCommand;
import kr.hhplus.be.server.product.domain.IProductRepository;
import kr.hhplus.be.server.product.domain.PopularProductCacheManager;
import kr.hhplus.be.server.product.domain.ProductStock;
import kr.hhplus.be.server.product.dto.PopularProductInfo;
import kr.hhplus.be.server.product.dto.ProductInfo;
import kr.hhplus.be.server.product.dto.ValidatedProductInfo;
import kr.hhplus.be.server.product.service.ProductService;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static kr.hhplus.be.server.common.exception.ApiErrorCode.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


@SpringBootTest(classes = ServerApplication.class)
@Testcontainers
@Sql(scripts = {"/test-data.sql"})
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private PopularProductCacheManager popularProductCacheManager;

    @Autowired
    private DataBaseCleanUp dataBaseCleanUp;

    @BeforeEach
    public void setUp() {
        dataBaseCleanUp.execute();
    }

    @Test
    void 상품목록_조회가_정상적으로_동작한다() {
        // when
        List<ProductInfo> products = productService.getAllProducts();

        // then
        assertThat(products)
                .hasSize(2)
                .extracting("name", "price")
                .containsExactly(
                        tuple("테스트상품1", BigDecimal.valueOf(10000).setScale(2)),
                        tuple("테스트상품2", BigDecimal.valueOf(15000).setScale(2))
                );
    }


    @Test
    void 인기상품_조회시_판매량_순으로_5개가_반환된다() {
        // given
        popularProductCacheManager.refreshPopularProducts();

        // when
        List<PopularProductInfo> popularProducts = productService.getTopFivePopularProducts();

        // then
        AssertionsForInterfaceTypes.assertThat(popularProducts)
                .isNotEmpty()
                .hasSizeLessThanOrEqualTo(5);
    }


    @Test
    void 상품_검증시_상품정보가_정상적으로_반환된다() {
        // given
        Long productId = 1L;
        List<OrderCommand.Item> commands = List.of(
                new OrderCommand.Item(productId, null, 5)
        );

        // when
        List<ValidatedProductInfo> validatedProducts = productService.validateProducts(commands);

        // then
        AssertionsForInterfaceTypes.assertThat(validatedProducts)
                .hasSize(1)
                .allSatisfy(product -> {
                    AssertionsForClassTypes.assertThat(product.product().getId()).isEqualTo(productId);
                    AssertionsForClassTypes.assertThat(product.quantity()).isEqualTo(5);
                });
    }

    @Test
    void 존재하지_않는_상품_검증시_NOT_FOUND_예외가_발생한다() {
        // given
        Long nonExistentProductId = 999L;
        List<OrderCommand.Item> commands = List.of(
                new OrderCommand.Item(nonExistentProductId, null, 5)
        );

        // when & then
        assertThatThrownBy(() -> productService.validateProducts(commands))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiErrorCode", NOT_FOUND);
    }

    @Test
    @Transactional
    void 재고_차감시_재고수량이_정상적으로_차감된다() {
        // given
        Long productId = 1L;
        int deductQuantity = 5;
        ProductStock beforeStock = productRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));
        int initialStock = beforeStock.getQuantity();

        List<OrderCommand.Item> commands = List.of(
                new OrderCommand.Item(productId, null, deductQuantity)
        );

        // when
        productService.deductStock(commands);

        // then
        ProductStock afterStock = productRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("재고 정보가 없습니다."));

        AssertionsForClassTypes.assertThat(afterStock.getQuantity())
                .isEqualTo(initialStock - deductQuantity);
    }

    @Test
    void 동시에_재고_차감시_정합성이_유지된다() throws InterruptedException {
        // given
        int threadCount = 5;
        Long productId = 1L;
        int deductQuantity = 2;

        ProductStock beforeStock = productRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("테스트 데이터가 없습니다."));
        int initialStock = beforeStock.getQuantity();

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    // 각 스레드에서 별도의 트랜잭션으로 실행
                    productService.deductStock(List.of(
                            new OrderCommand.Item(productId, null, deductQuantity)
                    ));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        // then
        latch.await(5, TimeUnit.SECONDS);
        AssertionsForClassTypes.assertThat(successCount.get()).isEqualTo(threadCount);

        Thread.sleep(100);  // 모든 스레드 종료 대기

        ProductStock afterStock = productRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("재고 정보가 없습니다."));

        AssertionsForClassTypes.assertThat(afterStock.getQuantity())
                .isEqualTo(initialStock - (deductQuantity * threadCount));
    }
}