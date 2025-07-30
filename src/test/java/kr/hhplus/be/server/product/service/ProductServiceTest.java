package kr.hhplus.be.server.product.service;


import kr.hhplus.be.server.order.domain.IOrderRepository;
import kr.hhplus.be.server.product.domain.IProductRepository;
import kr.hhplus.be.server.product.dto.PopularProductInfo;
import kr.hhplus.be.server.product.dto.ProductInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private IProductRepository productRepository;

    @Mock
    private IOrderRepository orderRepository;

    @InjectMocks
    private ProductService productService;


    @Test
    void 상품_목록_조회시_상품이_없으면_빈_List를_반환한다() {
        // given
        when(productRepository.findAll()).thenReturn(null); // 또는 Collections.emptyList()

        // when
        List<ProductInfo> result = productService.getAllProducts();

        // then
        assertThat(result).isNotNull();         // null 아님
        assertThat(result).isEmpty();           // 비어 있음
        verify(productRepository).findAll();    // 해당 메서드 호출 검증
    }

    @Test
    void 인기상품이_없으면_빈_리스트가_반환된다() {
        // given
        when(orderRepository.findTopFivePopularProducts()).thenReturn(List.of());

        // when
        List<PopularProductInfo> result = productService.getTopFivePopularProducts();

        // then
        assertThat(result).isEmpty();
    }
}