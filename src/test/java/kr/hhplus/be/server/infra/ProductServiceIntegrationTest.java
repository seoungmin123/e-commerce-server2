package kr.hhplus.be.server.infra;

import kr.hhplus.be.server.product.dto.PopularProductInfo;
import kr.hhplus.be.server.product.dto.ProductInfo;
import kr.hhplus.be.server.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;


@SpringBootTest
@Sql(scripts = {"file:./init/01-cleanup.sql", "file:./init/03-test-data.sql"})
class ProductServiceIntegrationTest {
    @Autowired
    private ProductService productService;
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
    void 인기상품_조회시_판매량_순으로_정렬되어_조회된다() {
        // given - test-data.sql의 주문 데이터 기준

        // when
        List<PopularProductInfo> products = productService.getTopFivePopularProducts();

        // then
        assertThat(products)
                .hasSize(2)
                .extracting("productId", "name", "totalQuantity")
                .containsExactly(
                        tuple(1L, "테스트상품1", 2),
                        tuple(2L, "테스트상품2", 1)
                );
    }
}